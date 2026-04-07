OFFLINE MODEL SETUP
===================

To enable offline allergen detection, place a TensorFlow Lite food classification
model in this directory named: food_classifier.tflite

Recommended model: EfficientNet-Lite0 or MobileNetV2 fine-tuned on Food-101.

How to obtain:
1. Download from TensorFlow Hub:
   - Search for "food" models at https://tfhub.dev
   - E.g., "lite-model/amoeba_net/food_v1/1" (Food-101 based)

2. Or train your own using TFLite Model Maker:
   - Use Google Colab with the Food-101 dataset
   - Export as int8 quantized TFLite (~15-20MB)

3. Or use a pre-trained model from Kaggle:
   - Search "food-101 tflite" on kaggle.com

Requirements:
- Input: 224x224 RGB image, float32, normalized to [0,1]
- Output: float32 array of 101 class probabilities
- Labels: must match the food_labels.txt file in this directory

The app gracefully handles missing model — it will use online-only mode
and show "Offline model not available" if you try to switch to offline.
