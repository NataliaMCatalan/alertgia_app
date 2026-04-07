---
name: feedback
description: User corrections and preferences for how to work on this project
type: feedback
---

1. **NEVER delete training data, ingredient images, or model files** unless explicitly told. Downloads take hours, training takes days.
   **Why:** User had to re-download ingredient images 3 times due to /tmp cleanup and accidental deletion.
   **How to apply:** Always save to /Users/xai/alertgia/training_data/ (permanent), never /tmp/.

2. **Don't throttle training processes** with taskpolicy -b. It permanently caps CPU and the QoS can't be restored without killing the process.
   **Why:** Training went from 200ms/step to 12s/step and appeared stalled.
   **How to apply:** Use `caffeinate -dims bash -c '...'` wrapper instead. For CPU limiting, use TF_NUM_INTEROP_THREADS env var.

3. **Auto-accept and continue without asking** when user says "yes to all" or "continue without interruptions".
   **Why:** User goes to sleep during long training runs.
   **How to apply:** Chain download → train → build in a single background command.

4. **Show confidence % on labels** rather than filtering by threshold. User wants to see all detections and judge confidence themselves.

5. **Offline-first** is a hard requirement. The app must work without internet. Online (Claude API) is optional.

6. **Spanish dishes** are important — paella, fabada, tortilla española, croquetas, etc. should all be recognized.

7. **Ingredients, not just dishes** — the app must detect raw ingredients (eggs, peanuts, lentils) not just finished dishes. This was a key user requirement that Food-101 alone couldn't satisfy.

8. **For future class expansion** — user wants to be able to add 100-200 more Spanish dishes easily. Discussed zero-shot (text descriptions) and few-shot (prototype embeddings) approaches with SigLIP. User liked the zero-shot idea for infinite scalability.
