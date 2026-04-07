#!/bin/bash
# Prevent any form of sleep during training
source /Users/xai/alertgia/ml_env/bin/activate

# Keep-alive loop in background: touches a file every 30s to prevent idle detection
while true; do
    touch /tmp/training_keepalive
    sleep 30
done &
KEEPALIVE_PID=$!

# Run training
TF_NUM_INTEROP_THREADS=4 TF_NUM_INTRAOP_THREADS=4 python /Users/xai/alertgia/train_food_ingredients_v2.py

# Cleanup
kill $KEEPALIVE_PID 2>/dev/null
