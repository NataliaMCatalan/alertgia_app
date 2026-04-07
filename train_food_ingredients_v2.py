"""
Train EfficientNetV2B2 on Food-101 + real ingredient images.
Fix: use class weights instead of oversampling to handle imbalance.
"""
import tensorflow as tf
import tensorflow_datasets as tfds
import numpy as np
import os

IMG_SIZE = 260
BATCH_SIZE = 32
OUTPUT_MODEL = "app/src/main/assets/food_ingredients_classifier.tflite"
OUTPUT_LABELS = "app/src/main/assets/food_ingredients_labels.txt"
INGREDIENTS_DIR = "/Users/xai/alertgia/training_data/ingredients"

print("TensorFlow version:", tf.__version__)

# =====================================================
# STEP 1: Load Food-101
# =====================================================
print("\n=== Loading Food-101 ===")
(ds_food_train, ds_food_val), food_info = tfds.load(
    'food101', split=['train', 'validation'], as_supervised=True, with_info=True)
food_labels = list(food_info.features['label'].names)
print(f"Food-101: {len(food_labels)} classes")

# =====================================================
# STEP 2: Load ingredients
# =====================================================
print("\n=== Loading ingredients ===")

# Re-download ingredients if /tmp was cleaned
if not os.path.exists(INGREDIENTS_DIR) or len(os.listdir(INGREDIENTS_DIR)) < 10:
    print("Ingredient images not found! Please re-run download_missing_ingredients.py first.")
    # Try to continue with Food-101 only
    ingredient_labels = []
else:
    ingredient_labels = sorted([
        d for d in os.listdir(INGREDIENTS_DIR)
        if os.path.isdir(os.path.join(INGREDIENTS_DIR, d))
        and len([f for f in os.listdir(os.path.join(INGREDIENTS_DIR, d))
                 if f.lower().endswith(('.jpg', '.jpeg'))]) >= 10
    ])

print(f"Ingredient classes: {len(ingredient_labels)}")

all_labels = food_labels + ingredient_labels
NUM_CLASSES = len(all_labels)
print(f"Total: {NUM_CLASSES}")

os.makedirs(os.path.dirname(OUTPUT_LABELS), exist_ok=True)
with open(OUTPUT_LABELS, 'w') as f:
    for label in all_labels:
        f.write(label + '\n')

# =====================================================
# STEP 3: Build datasets WITHOUT oversampling
# =====================================================
print("\n=== Building datasets ===")

def preprocess_train(image, label):
    image = tf.image.resize(image, [IMG_SIZE + 32, IMG_SIZE + 32])
    image = tf.image.random_crop(image, [IMG_SIZE, IMG_SIZE, 3])
    image = tf.image.random_flip_left_right(image)
    image = tf.image.random_brightness(image, 0.2)
    image = tf.image.random_contrast(image, 0.8, 1.2)
    image = tf.clip_by_value(image, 0.0, 255.0)
    image = tf.keras.applications.efficientnet_v2.preprocess_input(image)
    return image, label

def preprocess_val(image, label):
    image = tf.image.resize(image, [IMG_SIZE, IMG_SIZE])
    image = tf.keras.applications.efficientnet_v2.preprocess_input(tf.cast(image, tf.float32))
    return image, label

def load_and_preprocess(file_path, label, is_training=True):
    image = tf.io.read_file(file_path)
    image = tf.image.decode_jpeg(image, channels=3, try_recover_truncated=True)
    image.set_shape([None, None, 3])
    image = tf.cast(image, tf.float32)
    if is_training:
        image = tf.image.resize(image, [IMG_SIZE + 32, IMG_SIZE + 32])
        image = tf.image.random_crop(image, [IMG_SIZE, IMG_SIZE, 3])
        image = tf.image.random_flip_left_right(image)
        image = tf.image.random_brightness(image, 0.2)
        image = tf.image.random_contrast(image, 0.8, 1.2)
    else:
        image = tf.image.resize(image, [IMG_SIZE, IMG_SIZE])
    image = tf.clip_by_value(image, 0.0, 255.0)
    image = tf.keras.applications.efficientnet_v2.preprocess_input(image)
    return image, label

# Food-101
ds_food_train_p = ds_food_train.map(preprocess_train, num_parallel_calls=tf.data.AUTOTUNE)
ds_food_val_p = ds_food_val.map(preprocess_val, num_parallel_calls=tf.data.AUTOTUNE)

# Ingredients - NO repeat, just load once
train_files, train_lbls, val_files, val_lbls = [], [], [], []
class_counts = {}

for idx, class_name in enumerate(ingredient_labels):
    class_idx = len(food_labels) + idx
    class_dir = os.path.join(INGREDIENTS_DIR, class_name)
    files = sorted([
        os.path.join(class_dir, f) for f in os.listdir(class_dir)
        if f.lower().endswith(('.jpg', '.jpeg'))
    ])
    split = int(len(files) * 0.8)
    train_files.extend(files[:split])
    train_lbls.extend([class_idx] * split)
    val_files.extend(files[split:])
    val_lbls.extend([class_idx] * (len(files) - split))
    class_counts[class_idx] = split

print(f"Ingredient train: {len(train_files)}, val: {len(val_files)}")

# Compute class weights to handle imbalance
# Food-101: ~750 images per class, Ingredients: ~50-150 per class
total_samples = 75750 + len(train_files)  # food + ingredients
avg_per_class = total_samples / NUM_CLASSES

class_weight = {}
for i in range(len(food_labels)):
    class_weight[i] = avg_per_class / 750  # ~0.6 for food classes
for idx, count in class_counts.items():
    class_weight[idx] = min(avg_per_class / max(count, 1), 5.0)  # cap at 5x

print(f"Class weight range: {min(class_weight.values()):.2f} - {max(class_weight.values()):.2f}")

if len(train_files) > 0:
    ds_ingr_train = tf.data.Dataset.from_tensor_slices(
        (train_files, tf.constant(train_lbls, dtype=tf.int64))
    ).map(lambda f, l: load_and_preprocess(f, l, True),
          num_parallel_calls=tf.data.AUTOTUNE
    ).apply(tf.data.experimental.ignore_errors())

    ds_ingr_val = tf.data.Dataset.from_tensor_slices(
        (val_files, tf.constant(val_lbls, dtype=tf.int64))
    ).map(lambda f, l: load_and_preprocess(f, l, False),
          num_parallel_calls=tf.data.AUTOTUNE
    ).apply(tf.data.experimental.ignore_errors())

    ds_train = ds_food_train_p.concatenate(ds_ingr_train).shuffle(15000).batch(BATCH_SIZE).prefetch(tf.data.AUTOTUNE)
    ds_val = ds_food_val_p.concatenate(ds_ingr_val).batch(BATCH_SIZE).prefetch(tf.data.AUTOTUNE)
else:
    ds_train = ds_food_train_p.shuffle(10000).batch(BATCH_SIZE).prefetch(tf.data.AUTOTUNE)
    ds_val = ds_food_val_p.batch(BATCH_SIZE).prefetch(tf.data.AUTOTUNE)

# =====================================================
# STEP 4: Build and train
# =====================================================
print(f"\n=== Building EfficientNetV2B2 ({NUM_CLASSES} classes) ===")

base_model = tf.keras.applications.EfficientNetV2B2(
    input_shape=(IMG_SIZE, IMG_SIZE, 3), include_top=False, weights='imagenet')
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

print("\n=== Phase 1: Transfer learning (12 epochs) ===")
model.fit(ds_train, validation_data=ds_val, epochs=12, class_weight=class_weight,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=3, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ])

print("\n=== Phase 2: Fine-tune top 80 layers (12 epochs) ===")
base_model.trainable = True
for layer in base_model.layers[:-80]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=3e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(ds_train, validation_data=ds_val, epochs=12, class_weight=class_weight,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=3, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ])

print("\n=== Phase 3: Full fine-tune (10 epochs) ===")
for layer in base_model.layers:
    layer.trainable = True

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(ds_train, validation_data=ds_val, epochs=10, class_weight=class_weight,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1, min_lr=1e-7),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ])

print("\nFinal evaluation...")
loss, accuracy = model.evaluate(ds_val)
print(f"Validation accuracy: {accuracy:.4f}")

# Save Keras model so we can re-export if TFLite conversion goes wrong
model.save("/Users/xai/alertgia/training_data/food_ingredients_keras_model.keras")
print("Keras model saved to training_data/")

print("\nConverting to TFLite (float16)...")
def representative_dataset():
    for images, _ in ds_val.take(300):
        for image in images:
            yield [tf.expand_dims(image, 0)]

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
converter.target_spec.supported_types = [tf.float16]

tflite_model = converter.convert()
with open(OUTPUT_MODEL, 'wb') as f:
    f.write(tflite_model)

size_mb = os.path.getsize(OUTPUT_MODEL) / (1024*1024)
print(f"\nModel saved: {size_mb:.1f} MB, {NUM_CLASSES} classes")

# Verify - check it doesn't always predict the same class
interpreter = tf.lite.Interpreter(model_path=OUTPUT_MODEL)
interpreter.allocate_tensors()
inp = interpreter.get_input_details()[0]
out = interpreter.get_output_details()[0]

with open(OUTPUT_LABELS) as f:
    labels = [l.strip() for l in f.readlines()]

predictions = set()
for _ in range(10):
    test = np.random.rand(1, IMG_SIZE, IMG_SIZE, 3).astype(np.float32) * 2 - 1
    interpreter.set_tensor(inp['index'], test)
    interpreter.invoke()
    output = interpreter.get_tensor(out['index'])
    top = labels[output[0].argmax()]
    predictions.add(top)

if len(predictions) <= 2:
    print(f"WARNING: Model always predicts {predictions} — likely overfit!")
else:
    print(f"Sanity check passed: {len(predictions)} different predictions on random noise")

print("\nDone!")
