---
name: architecture
description: Tech stack, project structure, and key architectural decisions for Alertgia Android app
type: project
---

## Tech Stack
- **Kotlin + Jetpack Compose** (Material 3)
- **CameraX** for camera (ImageAnalysis for frame capture)
- **Room** database for user profiles + allergies
- **Hilt** for dependency injection
- **PyTorch Mobile** for SigLIP2 inference (org.pytorch:pytorch_android:2.1.0)
- **TensorFlow Lite** for COCO object detector + EfficientNet models
- **ML Kit** for barcode scanning + OCR text recognition
- **Google Play Services Location** for nearby places
- **DataStore** for language/TTS preferences
- **Retrofit + OkHttp** for Claude API (online mode)

## Key Architecture
- **MVVM** with StateFlow
- **FoodClassifier.kt** — abstraction over TFLite and PyTorch backends, model switching
- **PytorchClassifier.kt** — SigLIP2 inference wrapper
- **ObjectDetector.kt** — COCO SSD MobileNet for bounding box detection
- **LocalModelAnalysisStrategy.kt** — two-stage pipeline: detect objects → classify food → map allergens
- **AllergenAnalysisRepository.kt** — coordinates online (Claude) vs offline (local) strategies
- **FoodToAllergenMapper.kt** — 400+ food→allergen mappings
- **CameraViewModel.kt** — dual-path frame processing: full analysis + fast tracking between frames

## Product Flavors (build.gradle.kts)
- `full` — includes TFLite models + PyTorch
- `pytorch` — SigLIP2 only, has its own FoodClassifier.kt at app/src/pytorch/

## Key Directories
- `app/src/main/` — shared code
- `app/src/full/` — full flavor (FoodClassifier with TFLite, extra model assets)
- `app/src/pytorch/` — pytorch flavor (FoodClassifier without TFLite imports)
- `app/src/main/assets/` — shared assets (SigLIP2 model, COCO detector, labels)
- `app/src/full/assets/` — TFLite models (food_classifier.tflite, food_ingredients_classifier.tflite)

## Build
- Gradle 8.11.1 (extracted at /tmp/gradle-install/)
- `assembleFullDebug` or `assemblePytorchDebug`
- ABI filter: arm64-v8a only
- `noCompress += "tflite"` and `noCompress += "pt"` in androidResources
- ANTHROPIC_API_KEY in local.properties (not committed)
