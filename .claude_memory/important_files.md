---
name: important_files
description: Files that must NOT be deleted - they took hours/days to create
type: feedback
---

## DO NOT DELETE THESE FILES

### Training Data (hours of Bing downloads)
- `/Users/xai/alertgia/training_data/ingredients/` — 344 folders, 15,755 images
- `/Users/xai/alertgia/training_data/torchvision/` — Food-101 dataset (~5GB)

### Trained Models (hours of GPU/CPU training)
- `/Users/xai/alertgia/app/src/main/assets/food_sigclip_classifier.pt` — SigLIP2 B-32 (362MB)
- `/Users/xai/alertgia/app/src/full/assets/food_classifier.tflite` — EfficientNet dishes (11MB)
- `/Users/xai/alertgia/app/src/full/assets/food_ingredients_classifier.tflite` — EfficientNet ingredients (36MB, broken quantization)
- `/Users/xai/alertgia/app/src/main/assets/coco_ssd_mobilenet.tflite` — COCO detector (4MB)

### Keras Model (can re-export to TFLite without retraining)
- `/Users/xai/alertgia/training_data/food_ingredients_keras_model.keras`

### Checkpoints (can resume training)
- `/Users/xai/alertgia/training_data/sigclip_v2_checkpoints/`
- `/Users/xai/alertgia/training_data/sigclip_checkpoints/`

### Cached Model Weights (avoid re-downloading ~2GB)
- `/Users/xai/.cache/huggingface/` — SigLIP1, SigLIP2 weights

### Backup Models
- `/Users/xai/alertgia/app/src/main/assets_backup/` — original TFLite models

**Why:** User explicitly requested never deleting training data or models. Downloads from Bing take 6+ hours. Model training takes 10-24 hours. Re-downloading SigLIP weights takes significant time.
