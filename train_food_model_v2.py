"""
Train an improved MobileNetV2 model on Food-101 with better augmentation and more epochs.
"""
import tensorflow as tf
import tensorflow_datasets as tfds
import numpy as np
import os

IMG_SIZE = 224
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

# Stronger augmentation for training
def preprocess_train(image, label):
    image = tf.image.resize(image, [IMG_SIZE + 32, IMG_SIZE + 32])
    image = tf.image.random_crop(image, [IMG_SIZE, IMG_SIZE, 3])
    image = tf.image.random_flip_left_right(image)
    image = tf.image.random_brightness(image, 0.2)
    image = tf.image.random_contrast(image, 0.8, 1.2)
    image = tf.image.random_saturation(image, 0.8, 1.2)
    image = tf.clip_by_value(image, 0.0, 255.0)
    image = tf.cast(image, tf.float32) / 255.0
    return image, label

def preprocess_val(image, label):
    image = tf.image.resize(image, [IMG_SIZE, IMG_SIZE])
    image = tf.cast(image, tf.float32) / 255.0
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

print("Building model...")

base_model = tf.keras.applications.MobileNetV2(
    input_shape=(IMG_SIZE, IMG_SIZE, 3),
    include_top=False,
    weights='imagenet'
)
base_model.trainable = False

model = tf.keras.Sequential([
    base_model,
    tf.keras.layers.GlobalAveragePooling2D(),
    tf.keras.layers.Dropout(0.4),
    tf.keras.layers.Dense(256, activation='relu'),
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
print("\n=== Phase 1: Transfer learning (10 epochs) ===")
history1 = model.fit(
    ds_train,
    validation_data=ds_val,
    epochs=10,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=4, restore_best_weights=True, verbose=1)
    ]
)

# Phase 2: Fine-tune — unfreeze top 50 layers
print("\n=== Phase 2: Fine-tuning top 50 layers (10 epochs) ===")
base_model.trainable = True
for layer in base_model.layers[:-50]:
    layer.trainable = False

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=5e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

history2 = model.fit(
    ds_train,
    validation_data=ds_val,
    epochs=10,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=4, restore_best_weights=True, verbose=1)
    ]
)

# Phase 3: Full fine-tune with very low LR
print("\n=== Phase 3: Full model fine-tuning (5 epochs) ===")
for layer in base_model.layers:
    layer.trainable = True

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=1e-5),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

history3 = model.fit(
    ds_train,
    validation_data=ds_val,
    epochs=5,
    callbacks=[
        tf.keras.callbacks.ReduceLROnPlateau(patience=2, factor=0.5, verbose=1),
        tf.keras.callbacks.EarlyStopping(patience=3, restore_best_weights=True, verbose=1)
    ]
)

# Evaluate
print("\nFinal evaluation...")
loss, accuracy = model.evaluate(ds_val)
print(f"Validation accuracy: {accuracy:.4f}")

# Convert to TFLite with int8 quantization
print("\nConverting to TFLite with int8 quantization...")

def representative_dataset():
    for images, _ in ds_val.take(200):
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

print("\nDone! Improved model ready for deployment.")
