---
name: project_overview
description: Alertgia is an Android allergen detection app using camera + ML to identify food and flag allergens in real-time
type: project
---

## What is Alertgia
Android app that uses the phone camera to detect food/ingredients in real-time and alert users about allergens based on their profile.

## Current State (as of 2026-04-07)
- **Working app** installed on user's phone
- **344 classes**: 101 Food-101 dishes + 243 ingredients (fruits, vegetables, meats, fish, dairy, nuts, legumes, grains, spices, Spanish dishes, international dishes)
- **SigLIP2 ViT-B-32-256** model: 84.9% val accuracy, 362MB, ~2-3s inference
- **Bilingual** ES/EN with language toggle
- **Dietary restrictions**: Kosher, Halal, Vegan, Vegetarian, Pescatarian, Hindu, Buddhist, Lactose-Free, Low FODMAP, Keto, Paleo
- **Features**: live camera scan, photo capture, OCR menu scanner, QR reader, nearby pharmacy/hospital, TTS accessibility, confidence % on labels
- **21 allergen categories** mapped
- **Git repo**: https://github.com/NataliaMCatalan/alertgia_app (Git LFS for models)
- **Branch**: main (clean single commit with full history)

## APK Flavors
- `full` — all 3 models (TFLite + PyTorch), ~800MB
- `pytorch` — SigLIP2 only, ~482MB actual download size

## Known Issues
- Inference is 2-3 seconds (SigLIP2 on mobile CPU)
- Arrows don't track perfectly when camera moves fast
- EfficientNet ingredients model (food_ingredients_classifier.tflite) has int8 quantization issues — always predicts same class. The Keras model works fine, just the TFLite export is broken.
- Apple detected as "peach" sometimes (visual similarity)
