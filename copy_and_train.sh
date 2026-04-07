#!/bin/bash
set -e
source /Users/xai/alertgia/ml_env/bin/activate

echo "=== Waiting for download to finish ==="
while pgrep -f download_missing_ingredients > /dev/null 2>&1; do
    sleep 30
done

echo "=== Download complete, copying to permanent storage ==="
PERM_DIR="/Users/xai/alertgia/training_data/ingredients"

# Copy from /tmp to permanent, preserving what's there
for d in /tmp/ingredient_dataset/*/; do
    name=$(basename "$d")
    count=$(find "$d" -type f 2>/dev/null | wc -l)
    if [ "$count" -gt 0 ]; then
        mkdir -p "$PERM_DIR/$name"
        cp -n "$d"* "$PERM_DIR/$name/" 2>/dev/null || true
    fi
done

# Also handle Bing's query-named folders
for d in /tmp/bing_temp/*/; do
    [ -d "$d" ] || continue
    count=$(find "$d" -type f 2>/dev/null | wc -l)
    if [ "$count" -gt 0 ]; then
        echo "  Found extra: $(basename "$d") ($count files)"
    fi
done

echo "=== Converting all to clean RGB JPEG ==="
python3 -c "
import os
from PIL import Image
fixed = 0
removed = 0
for root, dirs, files in os.walk('$PERM_DIR'):
    for f in files:
        fp = os.path.join(root, f)
        try:
            img = Image.open(fp).convert('RGB')
            new_fp = os.path.splitext(fp)[0] + '.jpg'
            img.save(new_fp, 'JPEG', quality=90)
            if new_fp != fp:
                os.remove(fp)
            fixed += 1
        except:
            os.remove(fp)
            removed += 1
print(f'Converted {fixed}, removed {removed}')
"

echo "=== Final count ==="
total=0; classes=0
for d in "$PERM_DIR"/*/; do
    n=$(find "$d" -type f 2>/dev/null | wc -l)
    if [ "$n" -gt 0 ]; then
        classes=$((classes+1))
        total=$((total+n))
    fi
done
echo "$classes classes, $total images"

echo "=== Starting training ==="
TF_NUM_INTEROP_THREADS=4 TF_NUM_INTRAOP_THREADS=4 python /Users/xai/alertgia/train_food_ingredients_v2.py
