"""
Train EfficientNetV2B2 on Food-101 (TFDS) + real ingredient images (downloaded).
Outputs: food_ingredients_classifier.tflite + food_ingredients_labels.txt
"""
import tensorflow as tf
import tensorflow_datasets as tfds
import numpy as np
import os
import pathlib

IMG_SIZE = 260
BATCH_SIZE = 32
OUTPUT_MODEL = "app/src/main/assets/food_ingredients_classifier.tflite"
OUTPUT_LABELS = "app/src/main/assets/food_ingredients_labels.txt"
INGREDIENTS_DIR = "/tmp/ingredient_dataset"

print("TensorFlow version:", tf.__version__)

# =====================================================
# STEP 1: Load Food-101 from TFDS
# =====================================================
print("\n=== Loading Food-101 dataset ===")
(ds_food_train, ds_food_val), food_info = tfds.load(
    'food101',
    split=['train', 'validation'],
    as_supervised=True,
    with_info=True,
)
food_labels = list(food_info.features['label'].names)
print(f"Food-101: {len(food_labels)} classes, {food_info.splits['train'].num_examples} train")

# =====================================================
# STEP 2: Load ingredient images from disk
# =====================================================
print("\n=== Loading ingredient images ===")
ingredient_labels = sorted([
    d for d in os.listdir(INGREDIENTS_DIR)
    if os.path.isdir(os.path.join(INGREDIENTS_DIR, d))
    and len(os.listdir(os.path.join(INGREDIENTS_DIR, d))) >= 10
])
print(f"Ingredient classes with 10+ images: {len(ingredient_labels)}")
for il in ingredient_labels:
    count = len(os.listdir(os.path.join(INGREDIENTS_DIR, il)))
    print(f"  {il}: {count}")

# Combined labels: food-101 first, then ingredients
all_labels = food_labels + ingredient_labels
NUM_CLASSES = len(all_labels)
print(f"\nTotal classes: {NUM_CLASSES} ({len(food_labels)} dishes + {len(ingredient_labels)} ingredients)")

# Save labels
os.makedirs(os.path.dirname(OUTPUT_LABELS), exist_ok=True)
with open(OUTPUT_LABELS, 'w') as f:
    for label in all_labels:
        f.write(label + '\n')
print(f"Labels saved to {OUTPUT_LABELS}")

# =====================================================
# STEP 3: Build combined dataset
# =====================================================
print("\n=== Building combined dataset ===")

def preprocess_train(image, label):
    image = tf.image.resize(image, [IMG_SIZE + 40, IMG_SIZE + 40])
    image = tf.image.random_crop(image, [IMG_SIZE, IMG_SIZE, 3])
    image = tf.image.random_flip_left_right(image)
    image = tf.image.random_brightness(image, 0.3)
    image = tf.image.random_contrast(image, 0.7, 1.3)
    image = tf.image.random_saturation(image, 0.7, 1.3)
    image = tf.clip_by_value(image, 0.0, 255.0)
    image = tf.keras.applications.efficientnet_v2.preprocess_input(image)
    return image, label

def preprocess_val(image, label):
    image = tf.image.resize(image, [IMG_SIZE, IMG_SIZE])
    image = tf.keras.applications.efficientnet_v2.preprocess_input(tf.cast(image, tf.float32))
    return image, label

# Food-101 datasets
ds_food_train_processed = ds_food_train.map(preprocess_train, num_parallel_calls=tf.data.AUTOTUNE)
ds_food_val_processed = ds_food_val.map(preprocess_val, num_parallel_calls=tf.data.AUTOTUNE)

# Ingredient datasets from disk
def load_ingredient_dataset(split_ratio=0.8):
    """Load ingredient images and split into train/val."""
    train_images, train_labels = [], []
    val_images, val_labels = [], []

    for idx, class_name in enumerate(ingredient_labels):
        class_idx = len(food_labels) + idx  # offset by food-101 count
        class_dir = os.path.join(INGREDIENTS_DIR, class_name)
        files = sorted([
            os.path.join(class_dir, f) for f in os.listdir(class_dir)
            if f.lower().endswith(('.jpg', '.jpeg', '.png', '.webp'))
        ])
        split = int(len(files) * split_ratio)
        train_images.extend(files[:split])
        train_labels.extend([class_idx] * split)
        val_images.extend(files[split:])
        val_labels.extend([class_idx] * (len(files) - split))

    return train_images, train_labels, val_images, val_labels

train_files, train_lbls, val_files, val_lbls = load_ingredient_dataset()
print(f"Ingredient train: {len(train_files)}, val: {len(val_files)}")

def load_and_preprocess_image(file_path, label, is_training=True):
    image = tf.io.read_file(file_path)
    image = tf.image.decode_jpeg(image, channels=3, try_recover_truncated=True)
    image.set_shape([None, None, 3])  # Force 3-channel shape
    image = tf.cast(image, tf.float32)
    if is_training:
        image = tf.image.resize(image, [IMG_SIZE + 40, IMG_SIZE + 40])
        image = tf.image.random_crop(image, [IMG_SIZE, IMG_SIZE, 3])
        image = tf.image.random_flip_left_right(image)
        image = tf.image.random_brightness(image, 0.3)
        image = tf.image.random_contrast(image, 0.7, 1.3)
    else:
        image = tf.image.resize(image, [IMG_SIZE, IMG_SIZE])
    image = tf.clip_by_value(image, 0.0, 255.0)
    image = tf.keras.applications.efficientnet_v2.preprocess_input(image)
    return image, label

ds_ingr_train = tf.data.Dataset.from_tensor_slices((train_files, tf.constant(train_lbls, dtype=tf.int64)))
ds_ingr_train = ds_ingr_train.map(
    lambda f, l: load_and_preprocess_image(f, l, True),
    num_parallel_calls=tf.data.AUTOTUNE
).apply(tf.data.experimental.ignore_errors())
# Repeat ingredient data to balance with Food-101 (75k vs ~5k)
repeat_factor = max(1, 75000 // max(len(train_files), 1))
ds_ingr_train = ds_ingr_train.repeat(repeat_factor)
print(f"Ingredient repeat factor: {repeat_factor}x (to balance with Food-101)")

ds_ingr_val = tf.data.Dataset.from_tensor_slices((val_files, tf.constant(val_lbls, dtype=tf.int64)))
ds_ingr_val = ds_ingr_val.map(
    lambda f, l: load_and_preprocess_image(f, l, False),
    num_parallel_calls=tf.data.AUTOTUNE
).apply(tf.data.experimental.ignore_errors())

# Combine food + ingredients
ds_train = (ds_food_train_processed
    .concatenate(ds_ingr_train)
    .shuffle(20000)
    .batch(BATCH_SIZE)
    .prefetch(tf.data.AUTOTUNE))

ds_val = (ds_food_val_processed
    .concatenate(ds_ingr_val)
    .batch(BATCH_SIZE)
    .prefetch(tf.data.AUTOTUNE))

# =====================================================
# STEP 4: Build and train model
# =====================================================
print(f"\n=== Building EfficientNetV2B2 model ({NUM_CLASSES} classes) ===")

base_model = tf.keras.applications.EfficientNetV2B2(
    input_shape=(IMG_SIZE, IMG_SIZE, 3),
    include_top=False,
    weights='imagenet'
)
base_model.trainable = False

model = tf.keras.Sequential([
    base_model,
    tf.keras.layers.GlobalAveragePooling2D(),
    tf.keras.layers.Dropout(0.4),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.BatchNormalization(),
    tf.keras.layers.Dropout(0.3),
    tf.keras.layers.Dense(NUM_CLASSES, activation='softmax')
])

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.summary()

# Phase 1: Transfer learning
print("\n=== Phase 1: Transfer learning (12 epochs) ===")
model.fit(ds_train, validation_data=ds_val, epochs=12,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=3, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ])

# Phase 2: Fine-tune top 80 layers
print("\n=== Phase 2: Fine-tuning top 80 layers (12 epochs) ===")
base_model.trainable = True
for layer in base_model.layers[:-80]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=3e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(ds_train, validation_data=ds_val, epochs=12,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=3, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ])

# Phase 3: Full fine-tune
print("\n=== Phase 3: Full model fine-tuning (10 epochs) ===")
for layer in base_model.layers:
    layer.trainable = True

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(ds_train, validation_data=ds_val, epochs=10,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1, min_lr=1e-7),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ])

# Evaluate
print("\nFinal evaluation...")
loss, accuracy = model.evaluate(ds_val)
print(f"Validation accuracy: {accuracy:.4f}")

# Convert to TFLite
print("\nConverting to TFLite with int8 quantization...")

def representative_dataset():
    for images, _ in ds_val.take(300):
        for image in images:
            yield [tf.expand_dims(image, 0)]

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.representative_dataset = representative_dataset
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
converter.inference_input_type = tf.float32
converter.inference_output_type = tf.float32

tflite_model = converter.convert()

with open(OUTPUT_MODEL, 'wb') as f:
    f.write(tflite_model)

size_mb = os.path.getsize(OUTPUT_MODEL) / (1024 * 1024)
print(f"\nModel saved to {OUTPUT_MODEL}")
print(f"Model size: {size_mb:.1f} MB")
print(f"Classes: {NUM_CLASSES}")

# Verify
print("\nVerifying TFLite model...")
interpreter = tf.lite.Interpreter(model_path=OUTPUT_MODEL)
interpreter.allocate_tensors()
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
print(f"Input shape: {input_details[0]['shape']}, dtype: {input_details[0]['dtype']}")
print(f"Output shape: {output_details[0]['shape']}, dtype: {output_details[0]['dtype']}")

print(f"\nDone! Food+Ingredients model ready ({NUM_CLASSES} classes).")
