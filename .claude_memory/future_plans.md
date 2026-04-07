---
name: future_plans
description: Features discussed but not yet implemented
type: project
---

## Planned / Discussed

### Zero-shot + Few-shot Classification (HIGH PRIORITY)
- Use SigLIP2's text encoder for zero-shot classification
- Add new dishes/ingredients via text descriptions only (no training needed)
- Optionally boost accuracy with 5-10 photos per dish (prototype embeddings)
- This would allow adding 200+ Spanish dishes instantly
- **Status**: Discussed, user was very interested. Not yet implemented.

### ViT-L-16-SigLIP2-256 (Large model)
- User asked about using the Large version for higher accuracy
- ~300M params, ~500ms inference, potentially 95%+ accuracy
- Decided to test B-32 first, may revisit
- **Status**: Not started.

### Faster Inference
- Re-export with torch.inference_mode + optimize_for_mobile — partially done
- float16 quantization on mobile — done but model size didn't shrink much
- Vulkan GPU backend — not available for ViT on PyTorch Mobile
- ExecuTorch — Meta's new runtime, experimental
- **Status**: B-32 selected for speed (~3x faster than B-16). Still 2-3s on phone.

### Ingredients-101 Dataset
- Originally planned to use Ingredients-101 but discovered it's NOT individual ingredient images — it's just annotation labels on Food-101 dish photos
- Used Bing image downloads instead for real ingredient photos
- **Status**: Resolved with comprehensive_ingredients.py

### Release APK
- User asked about signed release build
- Would be smaller due to R8/ProGuard minification
- Not yet built
- **Status**: Not started.

### Better Arrow Tracking
- Arrows don't follow food perfectly when camera moves
- Dual-path pipeline (full analysis + fast tracking with COCO detector) implemented but still laggy
- **Status**: Implemented but imperfect.
