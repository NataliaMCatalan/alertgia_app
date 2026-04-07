# Alertgia - AI Allergen Detection App

## Project Summary
Android app that uses the phone camera to detect food and ingredients in real-time, flagging allergens based on user profiles. Uses SigLIP2 vision model for offline inference.

## Build Commands
```bash
# Set Android SDK
export ANDROID_HOME=/Users/xai/Library/Android/sdk

# Build pytorch-only APK (SigLIP2, ~482MB)
/tmp/gradle-install/gradle-8.11.1/bin/gradle -p /Users/xai/alertgia assemblePytorchDebug

# Build full APK (all models, ~800MB)
/tmp/gradle-install/gradle-8.11.1/bin/gradle -p /Users/xai/alertgia assembleFullDebug

# Install on phone
/Users/xai/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/pytorch/debug/app-pytorch-debug.apk
```

## Training
```bash
source /Users/xai/alertgia/ml_env/bin/activate
# SigLIP2 ViT-B-32 (current production model)
caffeinate -dims python train_sigclip_v2_fast.py
```

## Key Rules
- NEVER delete files in training_data/ or model files (*.pt, *.tflite, *.keras)
- NEVER use taskpolicy -b (permanently throttles processes)
- Always use caffeinate -dims for long-running training
- Save training data to /Users/xai/alertgia/training_data/ (permanent), never /tmp/
- App must work OFFLINE — online Claude API is optional
- Git LFS is configured for model files (*.pt, *.tflite, *.onnx, *.keras)

## GitHub
- Remote: https://github.com/NataliaMCatalan/alertgia_app
- Branch: main
- Git LFS enabled for model files
