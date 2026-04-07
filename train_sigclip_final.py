"""
Fine-tune SigLIP ViT-B/16 on Food-101 + Ingredients with BALANCED data.
Export to TFLite via ONNX.

Key fixes from previous attempts:
- Balance Food-101 (~100 imgs/class) to match ingredient counts
- Lower LR for Phase 1 (1e-4 not 1e-3 to avoid collapse)
- num_workers=0 for macOS
- Cosine LR with warmup
- Save checkpoints frequently
"""
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, Dataset
import torchvision.transforms as T
import torchvision.datasets as tvd
import open_clip
import os
import numpy as np
from PIL import Image

# Config
IMG_SIZE = 256  # SigLIP2-256 native resolution
BATCH_SIZE = 32
DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"
INGREDIENTS_DIR = "/Users/xai/alertgia/training_data/ingredients"
OUTPUT_ONNX = "/Users/xai/alertgia/training_data/food_sigclip.onnx"
OUTPUT_TFLITE = "app/src/main/assets/food_sigclip_classifier.tflite"
OUTPUT_LABELS = "app/src/main/assets/food_ingredients_labels.txt"
CHECKPOINT_DIR = "/Users/xai/alertgia/training_data/sigclip_checkpoints"

os.makedirs(CHECKPOINT_DIR, exist_ok=True)

print(f"Device: {DEVICE}")
print(f"PyTorch: {torch.__version__}")

# =====================================================
# STEP 1: Load datasets
# =====================================================
print("\n=== Loading Food-101 ===")
food101_train = tvd.Food101(root="/Users/xai/alertgia/training_data/torchvision", split="train", download=True)
food101_test = tvd.Food101(root="/Users/xai/alertgia/training_data/torchvision", split="test", download=True)
food101_classes = food101_train.classes
print(f"Food-101: {len(food101_classes)} classes")

print("\n=== Loading Ingredients ===")
ingredient_classes = sorted([
    d for d in os.listdir(INGREDIENTS_DIR)
    if os.path.isdir(os.path.join(INGREDIENTS_DIR, d))
    and len([f for f in os.listdir(os.path.join(INGREDIENTS_DIR, d))
             if f.lower().endswith(('.jpg', '.jpeg'))]) >= 10
])
print(f"Ingredients: {len(ingredient_classes)} classes")

all_labels = food101_classes + ingredient_classes
NUM_CLASSES = len(all_labels)
print(f"Total: {NUM_CLASSES} classes")

# Save labels
os.makedirs(os.path.dirname(OUTPUT_LABELS), exist_ok=True)
with open(OUTPUT_LABELS, 'w') as f:
    for label in all_labels:
        f.write(label + '\n')

# =====================================================
# STEP 2: Balanced dataset
# =====================================================
print("\n=== Building balanced dataset ===")

# Find min images per ingredient class
ingr_counts = []
for cls in ingredient_classes:
    cls_dir = os.path.join(INGREDIENTS_DIR, cls)
    count = len([f for f in os.listdir(cls_dir) if f.lower().endswith(('.jpg', '.jpeg'))])
    ingr_counts.append(count)

median_ingr = sorted(ingr_counts)[len(ingr_counts)//2]
MAX_PER_CLASS = min(median_ingr * 2, 150)  # Cap Food-101 to ~2x median ingredient count
print(f"Ingredient median: {median_ingr}, capping Food-101 at {MAX_PER_CLASS}/class")

class BalancedFoodDataset(Dataset):
    def __init__(self, food101_dataset, ingredients_dir, ingredient_classes,
                 food101_classes, transform, is_train=True, max_per_food_class=150):
        self.transform = transform
        self.samples = []

        # Build Food-101 class -> indices map
        class_indices = {}
        for idx in range(len(food101_dataset)):
            label = food101_dataset._labels[idx]
            if label not in class_indices:
                class_indices[label] = []
            class_indices[label].append(idx)

        # Subsample Food-101 to max_per_food_class
        import random
        random.seed(42)
        for label, indices in class_indices.items():
            if is_train:
                selected = random.sample(indices, min(len(indices), max_per_food_class))
            else:
                selected = indices  # keep full test set
            for idx in selected:
                img_path = str(food101_dataset._image_files[idx])
                self.samples.append((img_path, label))

        food_count = len(self.samples)
        print(f"  Food-101 {'train' if is_train else 'val'}: {food_count} images ({food_count//len(food101_classes)}/class avg)")

        # Add ingredients
        food_offset = len(food101_classes)
        ingr_count = 0
        for i, class_name in enumerate(ingredient_classes):
            class_dir = os.path.join(ingredients_dir, class_name)
            files = sorted([
                os.path.join(class_dir, f)
                for f in os.listdir(class_dir)
                if f.lower().endswith(('.jpg', '.jpeg'))
            ])
            split = int(len(files) * 0.85)
            selected = files[:split] if is_train else files[split:]
            label_idx = food_offset + i
            for fp in selected:
                self.samples.append((fp, label_idx))
            ingr_count += len(selected)

        print(f"  Ingredients {'train' if is_train else 'val'}: {ingr_count} images")
        print(f"  Total: {len(self.samples)}")

        # Shuffle
        random.shuffle(self.samples)

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        try:
            image = Image.open(path).convert('RGB')
        except:
            image = Image.new('RGB', (IMG_SIZE, IMG_SIZE))
        image = self.transform(image)
        return image, label


train_transform = T.Compose([
    T.Resize((IMG_SIZE + 32, IMG_SIZE + 32)),
    T.RandomCrop(IMG_SIZE),
    T.RandomHorizontalFlip(),
    T.ColorJitter(brightness=0.3, contrast=0.3, saturation=0.3, hue=0.1),
    T.ToTensor(),
    T.Normalize(mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5])
])

val_transform = T.Compose([
    T.Resize((IMG_SIZE, IMG_SIZE)),
    T.ToTensor(),
    T.Normalize(mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5])
])

train_dataset = BalancedFoodDataset(
    food101_train, INGREDIENTS_DIR, ingredient_classes, food101_classes,
    transform=train_transform, is_train=True, max_per_food_class=MAX_PER_CLASS
)
val_dataset = BalancedFoodDataset(
    food101_test, INGREDIENTS_DIR, ingredient_classes, food101_classes,
    transform=val_transform, is_train=False, max_per_food_class=999999
)

train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True,
                          num_workers=0, drop_last=True)
val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False,
                        num_workers=0)

print(f"Train batches: {len(train_loader)}, Val batches: {len(val_loader)}")

# =====================================================
# STEP 3: Build SigLIP model
# =====================================================
print(f"\n=== Loading SigLIP2 ViT-B/16-256 ===")

model_clip, _, _ = open_clip.create_model_and_transforms(
    'ViT-B-16-SigLIP2-256', pretrained='webli'
)

class FoodClassifier(nn.Module):
    def __init__(self, clip_model, num_classes):
        super().__init__()
        self.visual = clip_model.visual
        with torch.no_grad():
            dummy = torch.randn(1, 3, IMG_SIZE, IMG_SIZE)
            feat = self.visual(dummy)
            self.feat_dim = feat.shape[-1]
        print(f"  Feature dim: {self.feat_dim}")

        self.classifier = nn.Sequential(
            nn.LayerNorm(self.feat_dim),
            nn.Dropout(0.2),
            nn.Linear(self.feat_dim, num_classes)
        )

    def forward(self, x):
        features = self.visual(x)
        return self.classifier(features)

model = FoodClassifier(model_clip, NUM_CLASSES).to(DEVICE)
total_params = sum(p.numel() for p in model.parameters())
print(f"  Total params: {total_params/1e6:.1f}M")

# =====================================================
# STEP 4: Training functions
# =====================================================
def train_epoch(model, loader, optimizer, criterion, device, log_every=100):
    model.train()
    total_loss, correct, total = 0, 0, 0
    for i, (images, labels) in enumerate(loader):
        images, labels = images.to(device), labels.to(device)
        optimizer.zero_grad()
        outputs = model(images)
        loss = criterion(outputs, labels)
        loss.backward()
        torch.nn.utils.clip_grad_norm_(model.parameters(), 1.0)
        optimizer.step()
        total_loss += loss.item()
        _, predicted = outputs.max(1)
        total += labels.size(0)
        correct += predicted.eq(labels).sum().item()
        if i % log_every == 0:
            print(f"    Step {i}/{len(loader)}, loss: {loss.item():.4f}, acc: {100.*correct/total:.1f}%")
    return total_loss / len(loader), 100. * correct / total

def evaluate(model, loader, criterion, device):
    model.eval()
    total_loss, correct, total = 0, 0, 0
    with torch.no_grad():
        for images, labels in loader:
            images, labels = images.to(device), labels.to(device)
            outputs = model(images)
            loss = criterion(outputs, labels)
            total_loss += loss.item()
            _, predicted = outputs.max(1)
            total += labels.size(0)
            correct += predicted.eq(labels).sum().item()
    return total_loss / len(loader), 100. * correct / total

criterion = nn.CrossEntropyLoss(label_smoothing=0.1)

# =====================================================
# Phase 1: Train classifier only (lower LR!)
# =====================================================
print("\n=== Phase 1: Train classifier head (8 epochs, lr=1e-4) ===")
for param in model.visual.parameters():
    param.requires_grad = False

optimizer = optim.AdamW(model.classifier.parameters(), lr=1e-4, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=8)
best_val_acc = 0

for epoch in range(8):
    train_loss, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_loss, val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/8: train_acc={train_acc:.1f}%, val_acc={val_acc:.1f}%")
    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), f"{CHECKPOINT_DIR}/best_phase1.pth")

print(f"  Best Phase 1: {best_val_acc:.1f}%")
model.load_state_dict(torch.load(f"{CHECKPOINT_DIR}/best_phase1.pth", weights_only=True))

# =====================================================
# Phase 2: Unfreeze top transformer blocks
# =====================================================
print("\n=== Phase 2: Fine-tune top 6 blocks (12 epochs, lr=2e-5) ===")

# Unfreeze top 6 blocks
blocks = list(model.visual.trunk.blocks) if hasattr(model.visual, 'trunk') else []
if blocks:
    for block in blocks[-6:]:
        for param in block.parameters():
            param.requires_grad = True
    trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
    print(f"  Trainable: {trainable/1e6:.1f}M params")
else:
    for param in model.visual.parameters():
        param.requires_grad = True
    print("  All visual params unfrozen (no trunk.blocks)")

optimizer = optim.AdamW(filter(lambda p: p.requires_grad, model.parameters()),
                        lr=2e-5, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=12)
patience = 0

for epoch in range(12):
    train_loss, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_loss, val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/12: train_acc={train_acc:.1f}%, val_acc={val_acc:.1f}%")
    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), f"{CHECKPOINT_DIR}/best_phase2.pth")
        patience = 0
    else:
        patience += 1
        if patience >= 5:
            print("  Early stopping!")
            break

print(f"  Best Phase 2: {best_val_acc:.1f}%")
if os.path.exists(f"{CHECKPOINT_DIR}/best_phase2.pth"):
    model.load_state_dict(torch.load(f"{CHECKPOINT_DIR}/best_phase2.pth", weights_only=True))
else:
    print("  No improvement in Phase 2, keeping Phase 1 weights")
    model.load_state_dict(torch.load(f"{CHECKPOINT_DIR}/best_phase1.pth", weights_only=True))

# =====================================================
# Phase 3: Full fine-tune
# =====================================================
print("\n=== Phase 3: Full fine-tune (8 epochs, lr=5e-6) ===")
for param in model.parameters():
    param.requires_grad = True

optimizer = optim.AdamW(model.parameters(), lr=5e-6, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=8)
patience = 0

for epoch in range(8):
    train_loss, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_loss, val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/8: train_acc={train_acc:.1f}%, val_acc={val_acc:.1f}%")
    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), f"{CHECKPOINT_DIR}/best_phase3.pth")
        patience = 0
    else:
        patience += 1
        if patience >= 4:
            print("  Early stopping!")
            break

best_path = f"{CHECKPOINT_DIR}/best_phase3.pth"
if not os.path.exists(best_path):
    best_path = f"{CHECKPOINT_DIR}/best_phase2.pth"
if not os.path.exists(best_path):
    best_path = f"{CHECKPOINT_DIR}/best_phase1.pth"
model.load_state_dict(torch.load(best_path, weights_only=True))
print(f"\nBest validation accuracy: {best_val_acc:.1f}%")

# =====================================================
# STEP 5: Export to ONNX
# =====================================================
print("\n=== Exporting to ONNX ===")
model.eval().cpu()
dummy_input = torch.randn(1, 3, IMG_SIZE, IMG_SIZE)
torch.onnx.export(
    model, dummy_input, OUTPUT_ONNX,
    input_names=['image'],
    output_names=['logits'],
    opset_version=17
)
onnx_size = os.path.getsize(OUTPUT_ONNX) / (1024*1024)
print(f"ONNX saved: {onnx_size:.1f} MB at {OUTPUT_ONNX}")

# =====================================================
# STEP 6: Convert ONNX to TFLite
# =====================================================
print("\n=== Converting ONNX to TFLite ===")
try:
    import subprocess
    subprocess.run(["pip", "install", "onnx2tf", "onnx", "sng4onnx", "onnxsim"],
                   check=True, capture_output=True)

    result = subprocess.run([
        "onnx2tf", "-i", OUTPUT_ONNX,
        "-o", "/Users/xai/alertgia/training_data/sigclip_tf",
        "-oiqt",  # output int8 quantized tflite
        "-ioqd", "uint8",  # input quantization dtype
    ], capture_output=True, text=True, timeout=600)
    print(result.stdout[-500:] if result.stdout else "")
    if result.returncode != 0:
        print(f"onnx2tf failed: {result.stderr[-500:]}")
        # Try without quantization
        result = subprocess.run([
            "onnx2tf", "-i", OUTPUT_ONNX,
            "-o", "/Users/xai/alertgia/training_data/sigclip_tf",
        ], capture_output=True, text=True, timeout=600)

    import shutil
    from pathlib import Path
    tf_dir = Path("/Users/xai/alertgia/training_data/sigclip_tf")
    tflite_files = list(tf_dir.glob("*.tflite"))
    if tflite_files:
        # Pick the float32 one if available
        target = None
        for f in tflite_files:
            if 'float32' in f.name:
                target = f
                break
        if not target:
            target = tflite_files[0]
        shutil.copy(str(target), OUTPUT_TFLITE)
        size = os.path.getsize(OUTPUT_TFLITE) / (1024*1024)
        print(f"TFLite saved: {size:.1f} MB")
    else:
        print("No tflite files found!")
except Exception as e:
    print(f"TFLite conversion error: {e}")
    print(f"ONNX model saved at {OUTPUT_ONNX} — convert manually if needed")

# =====================================================
# Sanity check
# =====================================================
print("\n=== Sanity check ===")
model.eval()
with open(OUTPUT_LABELS) as f:
    labels = [l.strip() for l in f.readlines()]

# Test on real ingredient images
ingr_dir = INGREDIENTS_DIR
correct_high = 0
tested = 0
for cls in sorted(os.listdir(ingr_dir))[:15]:
    cls_dir = os.path.join(ingr_dir, cls)
    if not os.path.isdir(cls_dir): continue
    files = [f for f in os.listdir(cls_dir) if f.endswith('.jpg')][:2]
    for f in files:
        img = Image.open(os.path.join(cls_dir, f)).convert('RGB')
        img_t = val_transform(img).unsqueeze(0)
        with torch.no_grad():
            out = model(img_t)
            probs = torch.softmax(out, dim=1)
            conf, idx = probs.max(1)
            pred = labels[idx.item()]
            c = conf.item()
        match = cls in pred or pred in cls
        tested += 1
        if match and c >= 0.5:
            correct_high += 1
        tag = '✓' if match else '✗'
        print(f"  {cls} -> {pred} ({c:.0%}) {tag}")

print(f"\nHigh-confidence correct (>=50%): {correct_high}/{tested}")
print(f"Best val accuracy: {best_val_acc:.1f}%")
print("\nDone!")
