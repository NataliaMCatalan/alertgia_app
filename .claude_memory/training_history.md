---
name: training_history
description: History of all training attempts, what worked, what failed, and lessons learned
type: project
---

## Lessons Learned

1. **EfficientNetV2 needs [-1,1] normalization**, not [0,1]. First v3 training was stuck at 1.5% because of wrong preprocessing. Fixed by using `tf.keras.applications.efficientnet_v2.preprocess_input()`.

2. **int8 quantization destroys models trained with class weights**. Both int8 and float16 TFLite quantization caused the EfficientNet ingredients model to always predict the same class. The Keras model worked fine. Root cause unclear but related to class-weighted loss + quantization interaction.

3. **Oversampling ingredients 15x caused overfitting**. First ingredients model always predicted "oyster" (95% confidence on random noise). Fix: use class weights instead of repetition, or balance by undersampling majority classes.

4. **tflite-model-maker is broken on Apple Silicon/Python 3.11**. Use plain TensorFlow instead.

5. **macOS suspends background processes** even with `caffeinate`. Solution: `caffeinate -dims bash -c 'command'` wrapping the entire script, plus a keepalive loop touching a file every 30s. `taskpolicy -b` should NEVER be used — it permanently throttles the process and the QoS can't be restored without killing and restarting.

6. **SigLIP2 is the right approach for food detection**. It already knows what all foods look like from pretraining on billions of image-text pairs. Only needs 10-20 images per class for the classifier head. Reached 84.9% on 344 classes with just 20 images per new ingredient class.

7. **ViT-B-32 vs ViT-B-16**: Same model size (~86M params, ~360MB), but B-32 is ~3x faster inference (fewer patches). Accuracy difference is minimal.

8. **ONNX export from PyTorch 2.11 is problematic** with SigLIP/ViT architectures. The new torch.export-based exporter fails. Use `dynamo=False` flag to force legacy TorchScript-based exporter: `torch.onnx.export(model, dummy, path, dynamo=False)`.

9. **onnx2tf dependency hell**: needs tf_keras, onnx_graphsurgeon, etc. and versions conflict. For PyTorch→TFLite conversion, `ai-edge-torch` (now `litert-torch`) works but downgrades PyTorch.

10. **Bing image downloader** gets heavily throttled. 80 images/class = very slow. 20 images/class is sufficient for SigLIP2 and 4x faster.

## Training Timeline
- v1 MobileNetV2 (Food-101): 68.9% → first working model
- v2 MobileNetV2+ (more epochs): 76.5%
- v3 EfficientNetV2B2 (Food-101): 85.6% → best dish-only model
- EfficientNet ingredients (broken quantization): 84.0% Keras, 0% TFLite
- SigLIP1 ViT-B-16 (166 classes): 93.2% → proved SigLIP approach works
- SigLIP2 ViT-B-32 (344 classes): 84.9% → current production model

## Training Environment
- Python 3.11 venv at /Users/xai/alertgia/ml_env/
- TensorFlow 2.21.0, PyTorch 2.11.0, open_clip 3.3.0
- Apple Silicon MPS for PyTorch, CPU for TensorFlow
- Food-101 cached at /Users/xai/alertgia/training_data/torchvision/
- Ingredient images at /Users/xai/alertgia/training_data/ingredients/ (344 classes, 15,755 images)
- SigLIP2 weights cached at /Users/xai/.cache/huggingface/
