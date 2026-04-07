---
name: models
description: All ML models trained for Alertgia - accuracy, size, location, and which work correctly
type: project
---

## Active Models

### SigLIP2 ViT-B-32-256 (CURRENT BEST)
- **File**: app/src/main/assets/food_sigclip_classifier.pt
- **Size**: 362 MB
- **Classes**: 344 (101 dishes + 243 ingredients)
- **Val accuracy**: 84.9%
- **Ingredient sanity check**: 100% (30/30)
- **Inference**: ~2-3s on mobile CPU
- **Input**: 256x256, normalized [-1,1] (mean=0.5, std=0.5)
- **Runtime**: PyTorch Mobile (org.pytorch:pytorch_android:2.1.0)
- **Trained with**: train_sigclip_v2_fast.py, balanced dataset (Food-101 capped at 150/class)
- **Checkpoint**: /Users/xai/alertgia/training_data/sigclip_v2_checkpoints/

### EfficientNetV2B2 Dishes-Only (FAST, RELIABLE)
- **File**: app/src/full/assets/food_classifier.tflite
- **Size**: 11 MB
- **Classes**: 101 (Food-101 only)
- **Val accuracy**: 85.6%
- **Inference**: ~30ms
- **Input**: 260x260, normalized [-1,1]
- **Runtime**: TFLite
- **Works correctly**: YES

### COCO SSD MobileNet (OBJECT DETECTOR)
- **File**: app/src/main/assets/coco_ssd_mobilenet.tflite
- **Size**: 4 MB
- **Purpose**: Bounding box detection for food positioning
- **Runtime**: TFLite

## Broken Models (DO NOT USE as-is)

### EfficientNetV2B2 Dishes+Ingredients
- **File**: app/src/full/assets/food_ingredients_classifier.tflite
- **Size**: 36 MB
- **Problem**: int8 AND float16 quantization both destroy the model — always predicts same class
- **Keras model works fine**: /Users/xai/alertgia/training_data/food_ingredients_keras_model.keras
- **Val accuracy before quantization**: 84.0%
- **Root cause**: TFLite quantization corrupts EfficientNet with class-weighted training

## Previous Models (for reference)
- v1 MobileNetV2: 68.9% accuracy, 2.7 MB (train_food_model.py)
- v2 MobileNetV2+: 76.5% accuracy, 3.0 MB (train_food_model_v2.py)
- v3 EfficientNetV2B2: 85.6% accuracy, 10.7 MB (train_food_model_v3.py)
- SigLIP1 ViT-B-16: 93.2% on 166 classes (but only dishes+65 ingredients)

## Labels Files
- food_labels.txt — 101 Food-101 class names
- food_ingredients_labels.txt — 344 combined class names (101+243)
- coco_labels.txt — 80 COCO detection classes
