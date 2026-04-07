"""
Train SigLIP2 ViT-B-32-256 (fast version) on Food-101 + Ingredients.
Export with inference_mode, optimize_for_mobile, and float16 quantization.
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
IMG_SIZE = 256
BATCH_SIZE = 32
DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"
INGREDIENTS_DIR = "/Users/xai/alertgia/training_data/ingredients"
OUTPUT_PT = "app/src/main/assets/food_sigclip_classifier.pt"
OUTPUT_LABELS = "app/src/main/assets/food_ingredients_labels.txt"
CHECKPOINT_DIR = "/Users/xai/alertgia/training_data/sigclip_v2_checkpoints"

os.makedirs(CHECKPOINT_DIR, exist_ok=True)

print(f"Device: {DEVICE}")

# =====================================================
# Load datasets (same balanced approach as v1)
# =====================================================
print("\n=== Loading Food-101 ===")
food101_train = tvd.Food101(root="/Users/xai/alertgia/training_data/torchvision", split="train", download=True)
food101_test = tvd.Food101(root="/Users/xai/alertgia/training_data/torchvision", split="test", download=True)
food101_classes = food101_train.classes

print("\n=== Loading Ingredients ===")
ingredient_classes = sorted([
    d for d in os.listdir(INGREDIENTS_DIR)
    if os.path.isdir(os.path.join(INGREDIENTS_DIR, d))
    and len([f for f in os.listdir(os.path.join(INGREDIENTS_DIR, d))
             if f.lower().endswith(('.jpg', '.jpeg'))]) >= 3
])

all_labels = food101_classes + ingredient_classes
NUM_CLASSES = len(all_labels)
print(f"Total: {NUM_CLASSES} classes ({len(food101_classes)} dishes + {len(ingredient_classes)} ingredients)")

os.makedirs(os.path.dirname(OUTPUT_LABELS), exist_ok=True)
with open(OUTPUT_LABELS, 'w') as f:
    for label in all_labels:
        f.write(label + '\n')

# =====================================================
# Balanced dataset
# =====================================================
import random
random.seed(42)

ingr_counts = []
for cls in ingredient_classes:
    cls_dir = os.path.join(INGREDIENTS_DIR, cls)
    count = len([f for f in os.listdir(cls_dir) if f.lower().endswith(('.jpg', '.jpeg'))])
    ingr_counts.append(count)

median_ingr = sorted(ingr_counts)[len(ingr_counts)//2]
MAX_PER_CLASS = min(median_ingr * 2, 150)
print(f"Capping Food-101 at {MAX_PER_CLASS}/class")

class BalancedFoodDataset(Dataset):
    def __init__(self, food101_dataset, is_train=True):
        self.transform = train_transform if is_train else val_transform
        self.samples = []

        class_indices = {}
        for idx in range(len(food101_dataset)):
            label = food101_dataset._labels[idx]
            if label not in class_indices:
                class_indices[label] = []
            class_indices[label].append(idx)

        for label, indices in class_indices.items():
            selected = random.sample(indices, min(len(indices), MAX_PER_CLASS)) if is_train else indices
            for idx in selected:
                self.samples.append((str(food101_dataset._image_files[idx]), label))

        food_offset = len(food101_classes)
        for i, class_name in enumerate(ingredient_classes):
            class_dir = os.path.join(INGREDIENTS_DIR, class_name)
            files = sorted([os.path.join(class_dir, f) for f in os.listdir(class_dir) if f.lower().endswith(('.jpg', '.jpeg'))])
            split = int(len(files) * 0.85)
            selected = files[:split] if is_train else files[split:]
            for fp in selected:
                self.samples.append((fp, food_offset + i))

        random.shuffle(self.samples)
        print(f"  {'Train' if is_train else 'Val'}: {len(self.samples)} samples")

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        try:
            image = Image.open(path).convert('RGB')
        except:
            image = Image.new('RGB', (IMG_SIZE, IMG_SIZE))
        return self.transform(image), label

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

train_dataset = BalancedFoodDataset(food101_train, is_train=True)
val_dataset = BalancedFoodDataset(food101_test, is_train=False)

train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True, num_workers=0, drop_last=True)
val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False, num_workers=0)

# =====================================================
# Model: ViT-B-32-SigLIP2-256 (FAST version)
# =====================================================
print(f"\n=== Loading SigLIP2 ViT-B-32-256 (fast) ===")

model_clip, _, _ = open_clip.create_model_and_transforms('ViT-B-32-SigLIP2-256', pretrained='webli')

class FoodClassifier(nn.Module):
    def __init__(self, clip_model, num_classes):
        super().__init__()
        self.visual = clip_model.visual
        with torch.no_grad():
            feat = self.visual(torch.randn(1, 3, IMG_SIZE, IMG_SIZE))
            self.feat_dim = feat.shape[-1]
        print(f"  Feature dim: {self.feat_dim}")
        self.classifier = nn.Sequential(
            nn.LayerNorm(self.feat_dim),
            nn.Dropout(0.2),
            nn.Linear(self.feat_dim, num_classes)
        )
    def forward(self, x):
        return self.classifier(self.visual(x))

model = FoodClassifier(model_clip, NUM_CLASSES).to(DEVICE)
print(f"  Params: {sum(p.numel() for p in model.parameters())/1e6:.1f}M")

# =====================================================
# Training
# =====================================================
def train_epoch(model, loader, optimizer, criterion, device):
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
        if i % 100 == 0:
            print(f"    Step {i}/{len(loader)}, loss: {loss.item():.4f}, acc: {100.*correct/total:.1f}%")
    return total_loss / len(loader), 100. * correct / total

def evaluate(model, loader, criterion, device):
    model.eval()
    correct, total = 0, 0
    with torch.no_grad():
        for images, labels in loader:
            images, labels = images.to(device), labels.to(device)
            _, predicted = model(images).max(1)
            total += labels.size(0)
            correct += predicted.eq(labels).sum().item()
    return 100. * correct / total

criterion = nn.CrossEntropyLoss(label_smoothing=0.1)
best_val_acc = 0

# Phase 1: classifier only
print("\n=== Phase 1: Classifier head (8 epochs, lr=1e-4) ===")
for p in model.visual.parameters(): p.requires_grad = False
optimizer = optim.AdamW(model.classifier.parameters(), lr=1e-4, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=8)

for epoch in range(8):
    _, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/8: train={train_acc:.1f}%, val={val_acc:.1f}%")
    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), f"{CHECKPOINT_DIR}/best_p1.pth")

print(f"  Best Phase 1: {best_val_acc:.1f}%")
model.load_state_dict(torch.load(f"{CHECKPOINT_DIR}/best_p1.pth", weights_only=True))

# Phase 2: top blocks
print("\n=== Phase 2: Top 6 blocks (10 epochs, lr=2e-5) ===")
blocks = list(model.visual.trunk.blocks) if hasattr(model.visual, 'trunk') else []
if blocks:
    for block in blocks[-6:]:
        for p in block.parameters(): p.requires_grad = True
else:
    for p in model.visual.parameters(): p.requires_grad = True

optimizer = optim.AdamW(filter(lambda p: p.requires_grad, model.parameters()), lr=2e-5, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=10)
patience = 0

for epoch in range(10):
    _, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/10: train={train_acc:.1f}%, val={val_acc:.1f}%")
    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), f"{CHECKPOINT_DIR}/best_p2.pth")
        patience = 0
    else:
        patience += 1
        if patience >= 4:
            print("  Early stopping!")
            break

best_path = f"{CHECKPOINT_DIR}/best_p2.pth"
if not os.path.exists(best_path):
    best_path = f"{CHECKPOINT_DIR}/best_p1.pth"
model.load_state_dict(torch.load(best_path, weights_only=True))
print(f"  Best Phase 2: {best_val_acc:.1f}%")

# Phase 3: full fine-tune
print("\n=== Phase 3: Full (6 epochs, lr=5e-6) ===")
for p in model.parameters(): p.requires_grad = True
optimizer = optim.AdamW(model.parameters(), lr=5e-6, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=6)
patience = 0

for epoch in range(6):
    _, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/6: train={train_acc:.1f}%, val={val_acc:.1f}%")
    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), f"{CHECKPOINT_DIR}/best_p3.pth")
        patience = 0
    else:
        patience += 1
        if patience >= 3:
            print("  Early stopping!")
            break

for p in [f"{CHECKPOINT_DIR}/best_p3.pth", f"{CHECKPOINT_DIR}/best_p2.pth", f"{CHECKPOINT_DIR}/best_p1.pth"]:
    if os.path.exists(p):
        model.load_state_dict(torch.load(p, weights_only=True))
        break

print(f"\nBest accuracy: {best_val_acc:.1f}%")

# =====================================================
# Export with all optimizations
# =====================================================
print("\n=== Exporting optimized model ===")
model.eval().cpu()

# 1. Trace with inference_mode
print("  Tracing with inference_mode...")
dummy = torch.randn(1, 3, IMG_SIZE, IMG_SIZE)
with torch.inference_mode():
    traced = torch.jit.trace(model, dummy)

# 2. Float16 quantization
print("  Quantizing to float16...")
traced_half = torch.quantization.quantize_dynamic(
    traced, {nn.Linear}, dtype=torch.float16
)

# 3. Optimize for mobile
print("  Optimizing for mobile...")
try:
    from torch.utils.mobile_optimizer import optimize_for_mobile
    optimized = optimize_for_mobile(traced_half)
    optimized._save_for_lite_interpreter(OUTPUT_PT)
    print("  Saved as lite interpreter format")
except Exception as e:
    print(f"  mobile_optimizer not available ({e}), saving standard TorchScript...")
    # Fallback: save float16 traced model
    traced_half.save(OUTPUT_PT)

size = os.path.getsize(OUTPUT_PT) / (1024*1024)
print(f"  Model saved: {size:.1f} MB")

# Sanity check
print("\n=== Sanity check ===")
with open(OUTPUT_LABELS) as f:
    labels = [l.strip() for l in f.readlines()]

model.eval()
correct = 0
tested = 0
for cls in sorted(os.listdir(INGREDIENTS_DIR))[:15]:
    cls_dir = os.path.join(INGREDIENTS_DIR, cls)
    if not os.path.isdir(cls_dir): continue
    files = [f for f in os.listdir(cls_dir) if f.endswith('.jpg')][:2]
    for f in files:
        img = Image.open(os.path.join(cls_dir, f)).convert('RGB')
        img_t = val_transform(img).unsqueeze(0)
        with torch.no_grad():
            probs = torch.softmax(model(img_t), dim=1)
            conf, idx = probs.max(1)
            pred = labels[idx.item()]
            c = conf.item()
        match = cls in pred or pred in cls
        if match: correct += 1
        tested += 1
        tag = '✓' if match else '✗'
        print(f"  {cls} -> {pred} ({c:.0%}) {tag}")

print(f"\nAccuracy: {correct}/{tested} ({100*correct/tested:.0f}%)")
print(f"Best val: {best_val_acc:.1f}%")
print("Done!")
