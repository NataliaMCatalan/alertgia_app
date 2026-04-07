"""
Train EfficientNet-Lite2 on Food-101 with aggressive augmentation for maximum accuracy.
"""
import tensorflow as tf
import tensorflow_datasets as tfds
import numpy as np
import os

IMG_SIZE = 260  # EfficientNet-B2 native resolution
BATCH_SIZE = 32
NUM_CLASSES = 101
OUTPUT_PATH = "app/src/main/assets/food_classifier.tflite"

print("TensorFlow version:", tf.__version__)
print("Loading Food-101 dataset...")

(ds_train, ds_val), ds_info = tfds.load(
    'food101',
    split=['train', 'validation'],
    as_supervised=True,
    with_info=True,
)

print(f"Training samples: {ds_info.splits['train'].num_examples}")
print(f"Validation samples: {ds_info.splits['validation'].num_examples}")

def preprocess_train(image, label):
    image = tf.image.resize(image, [IMG_SIZE + 40, IMG_SIZE + 40])
    image = tf.image.random_crop(image, [IMG_SIZE, IMG_SIZE, 3])
    image = tf.image.random_flip_left_right(image)
    image = tf.image.random_brightness(image, 0.3)
    image = tf.image.random_contrast(image, 0.7, 1.3)
    image = tf.image.random_saturation(image, 0.7, 1.3)
    image = tf.image.random_hue(image, 0.1)
    image = tf.clip_by_value(image, 0.0, 255.0)
    # EfficientNetV2 expects [-1, 1] range
    image = tf.keras.applications.efficientnet_v2.preprocess_input(image)
    return image, label

def preprocess_val(image, label):
    image = tf.image.resize(image, [IMG_SIZE, IMG_SIZE])
    # EfficientNetV2 expects [-1, 1] range
    image = tf.keras.applications.efficientnet_v2.preprocess_input(tf.cast(image, tf.float32))
    return image, label

ds_train = (ds_train
    .map(preprocess_train, num_parallel_calls=tf.data.AUTOTUNE)
    .shuffle(10000)
    .batch(BATCH_SIZE)
    .prefetch(tf.data.AUTOTUNE))

ds_val = (ds_val
    .map(preprocess_val, num_parallel_calls=tf.data.AUTOTUNE)
    .batch(BATCH_SIZE)
    .prefetch(tf.data.AUTOTUNE))

print("Building EfficientNetV2B2 model...")

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

# Phase 1: Transfer learning (frozen base)
print("\n=== Phase 1: Transfer learning (15 epochs) ===")
model.fit(
    ds_train,
    validation_data=ds_val,
    epochs=15,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=3, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ]
)

# Phase 2: Fine-tune top 80 layers
print("\n=== Phase 2: Fine-tuning top 80 layers (15 epochs) ===")
base_model.trainable = True
for layer in base_model.layers[:-80]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=3e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(
    ds_train,
    validation_data=ds_val,
    epochs=15,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=3, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ]
)

# Phase 3: Full fine-tune with very low LR
print("\n=== Phase 3: Full model fine-tuning (15 epochs) ===")
for layer in base_model.layers:
    layer.trainable = True

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

model.fit(
    ds_train,
    validation_data=ds_val,
    epochs=15,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1, min_lr=1e-7),
        tf.keras.callbacks.EarlyStopping(patience=5, restore_best_weights=True, verbose=1)
    ]
)

# Evaluate
print("\nFinal evaluation...")
loss, accuracy = model.evaluate(ds_val)
print(f"Validation accuracy: {accuracy:.4f}")

# Convert to TFLite with int8 quantization
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

os.makedirs(os.path.dirname(OUTPUT_PATH), exist_ok=True)
with open(OUTPUT_PATH, 'wb') as f:
    f.write(tflite_model)

size_mb = os.path.getsize(OUTPUT_PATH) / (1024 * 1024)
print(f"\nModel saved to {OUTPUT_PATH}")
print(f"Model size: {size_mb:.1f} MB")

# Verify
print("\nVerifying TFLite model...")
interpreter = tf.lite.Interpreter(model_path=OUTPUT_PATH)
interpreter.allocate_tensors()
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()
print(f"Input shape: {input_details[0]['shape']}, dtype: {input_details[0]['dtype']}")
print(f"Output shape: {output_details[0]['shape']}, dtype: {output_details[0]['dtype']}")

test_input = np.random.rand(1, IMG_SIZE, IMG_SIZE, 3).astype(np.float32)
interpreter.set_tensor(input_details[0]['index'], test_input)
interpreter.invoke()
output = interpreter.get_tensor(output_details[0]['index'])
print(f"Output sum (should be ~1.0): {output.sum():.4f}")

print("\nIMPORTANT: This model expects inputs in [-1, 1] range.")
print("Update FoodClassifier.kt to normalize: (pixel / 127.5) - 1.0")
print("\nDone! EfficientNetV2B2 model ready for deployment.")
