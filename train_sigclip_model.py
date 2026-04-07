"""
Fine-tune SigLIP ViT-B/16 vision encoder on Food-101 + Ingredients.
Export vision encoder + classifier head to TFLite for on-device inference.

Strategy:
1. Load pre-trained SigLIP vision encoder (already understands food concepts)
2. Add classification head for our 187 classes
3. Fine-tune with frozen base, then unfreeze
4. Export to ONNX -> TFLite (via tf-lite converter)

Since TFLite conversion from PyTorch is complex, we'll:
- Fine-tune in PyTorch
- Export to ONNX
- Convert ONNX to TFLite
"""
import torch
import torch.nn as nn
import torch.optim as optim
from torch.utils.data import DataLoader, Dataset, random_split
import torchvision.transforms as T
from torchvision.datasets import ImageFolder
import open_clip
import os
import numpy as np
from PIL import Image
from pathlib import Path
import shutil

# Config
IMG_SIZE = 224  # SigLIP ViT-B/16 uses 224
BATCH_SIZE = 32
DEVICE = "mps" if torch.backends.mps.is_available() else "cpu"  # Apple Silicon GPU
INGREDIENTS_DIR = "/tmp/ingredient_dataset"
COMBINED_DIR = "/tmp/combined_food_dataset"
OUTPUT_ONNX = "/tmp/food_sigclip.onnx"
OUTPUT_TFLITE = "app/src/main/assets/food_ingredients_classifier.tflite"
OUTPUT_LABELS = "app/src/main/assets/food_ingredients_labels.txt"

print(f"Device: {DEVICE}")
print(f"PyTorch: {torch.__version__}")

# =====================================================
# STEP 1: Prepare combined dataset directory
# =====================================================
print("\n=== Preparing combined dataset ===")

# We need Food-101 as an ImageFolder too
# Download if not already present via torchvision
import torchvision.datasets as tvd

print("Loading Food-101 via torchvision...")
food101_train = tvd.Food101(root="/tmp/torchvision_data", split="train", download=True)
food101_test = tvd.Food101(root="/tmp/torchvision_data", split="test", download=True)
food101_classes = food101_train.classes
print(f"Food-101: {len(food101_classes)} classes")

# Get ingredient classes
ingredient_classes = sorted([
    d for d in os.listdir(INGREDIENTS_DIR)
    if os.path.isdir(os.path.join(INGREDIENTS_DIR, d))
    and len([f for f in os.listdir(os.path.join(INGREDIENTS_DIR, d))
             if f.lower().endswith(('.jpg', '.jpeg', '.png', '.webp'))]) >= 10
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
# STEP 2: Create dataset
# =====================================================
print("\n=== Creating datasets ===")

class CombinedFoodDataset(Dataset):
    """Combines Food-101 (torchvision) with ingredient images from disk."""

    def __init__(self, food101_dataset, ingredients_dir, ingredient_classes,
                 food101_classes, transform=None, is_train=True):
        self.transform = transform
        self.samples = []

        # Add Food-101 samples
        for idx in range(len(food101_dataset)):
            img_path = str(food101_dataset._image_files[idx])
            label_idx = food101_dataset._labels[idx]
            self.samples.append((img_path, label_idx))

        # Add ingredient samples
        food_offset = len(food101_classes)
        for i, class_name in enumerate(ingredient_classes):
            class_dir = os.path.join(ingredients_dir, class_name)
            files = sorted([
                os.path.join(class_dir, f)
                for f in os.listdir(class_dir)
                if f.lower().endswith(('.jpg', '.jpeg', '.png', '.webp'))
            ])
            # Split 80/20
            split = int(len(files) * 0.8)
            if is_train:
                selected = files[:split]
            else:
                selected = files[split:]

            label_idx = food_offset + i
            for fp in selected:
                self.samples.append((fp, label_idx))

        # For training, oversample ingredients to balance
        if is_train:
            food_count = len(food101_dataset)
            ingr_count = len(self.samples) - food_count
            if ingr_count > 0 and food_count > ingr_count:
                repeat = food_count // ingr_count
                ingr_samples = self.samples[food_count:]
                for _ in range(repeat - 1):
                    self.samples.extend(ingr_samples)

        print(f"  {'Train' if is_train else 'Val'} samples: {len(self.samples)}")

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, idx):
        path, label = self.samples[idx]
        try:
            image = Image.open(path).convert('RGB')
        except:
            # Return a black image on error
            image = Image.new('RGB', (IMG_SIZE, IMG_SIZE))
        if self.transform:
            image = self.transform(image)
        return image, label


# SigLIP preprocessing
train_transform = T.Compose([
    T.Resize((IMG_SIZE + 32, IMG_SIZE + 32)),
    T.RandomCrop(IMG_SIZE),
    T.RandomHorizontalFlip(),
    T.ColorJitter(brightness=0.3, contrast=0.3, saturation=0.3, hue=0.1),
    T.ToTensor(),
    T.Normalize(mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5])  # SigLIP uses [-1, 1]
])

val_transform = T.Compose([
    T.Resize((IMG_SIZE, IMG_SIZE)),
    T.ToTensor(),
    T.Normalize(mean=[0.5, 0.5, 0.5], std=[0.5, 0.5, 0.5])
])

train_dataset = CombinedFoodDataset(
    food101_train, INGREDIENTS_DIR, ingredient_classes, food101_classes,
    transform=train_transform, is_train=True
)
val_dataset = CombinedFoodDataset(
    food101_test, INGREDIENTS_DIR, ingredient_classes, food101_classes,
    transform=val_transform, is_train=False
)

train_loader = DataLoader(train_dataset, batch_size=BATCH_SIZE, shuffle=True,
                          num_workers=0, pin_memory=True, drop_last=True)
val_loader = DataLoader(val_dataset, batch_size=BATCH_SIZE, shuffle=False,
                        num_workers=0, pin_memory=True)

# =====================================================
# STEP 3: Build model
# =====================================================
print(f"\n=== Building SigLIP model ({NUM_CLASSES} classes) ===")

# Load pre-trained SigLIP
model_clip, _, preprocess = open_clip.create_model_and_transforms(
    'ViT-B-16-SigLIP',
    pretrained='webli'
)

class FoodClassifierSigLIP(nn.Module):
    def __init__(self, clip_model, num_classes):
        super().__init__()
        self.visual = clip_model.visual
        # Get the output dimension
        with torch.no_grad():
            dummy = torch.randn(1, 3, IMG_SIZE, IMG_SIZE)
            feat = self.visual(dummy)
            feat_dim = feat.shape[-1]
        print(f"  Visual feature dim: {feat_dim}")

        self.classifier = nn.Sequential(
            nn.LayerNorm(feat_dim),
            nn.Dropout(0.3),
            nn.Linear(feat_dim, 512),
            nn.GELU(),
            nn.Dropout(0.2),
            nn.Linear(512, num_classes)
        )

    def forward(self, x):
        features = self.visual(x)
        return self.classifier(features)

model = FoodClassifierSigLIP(model_clip, NUM_CLASSES).to(DEVICE)

# Count parameters
total_params = sum(p.numel() for p in model.parameters())
trainable_params = sum(p.numel() for p in model.parameters() if p.requires_grad)
print(f"  Total params: {total_params/1e6:.1f}M")

# =====================================================
# STEP 4: Train
# =====================================================
def train_epoch(model, loader, optimizer, criterion, device):
    model.train()
    total_loss, correct, total = 0, 0, 0
    for batch_idx, (images, labels) in enumerate(loader):
        images, labels = images.to(device), labels.to(device)
        optimizer.zero_grad()
        outputs = model(images)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()
        total_loss += loss.item()
        _, predicted = outputs.max(1)
        total += labels.size(0)
        correct += predicted.eq(labels).sum().item()
        if batch_idx % 200 == 0:
            print(f"    Step {batch_idx}/{len(loader)}, loss: {loss.item():.4f}, acc: {100.*correct/total:.1f}%")
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

# Phase 1: Frozen visual encoder, train classifier only
print("\n=== Phase 1: Train classifier head (5 epochs) ===")
for param in model.visual.parameters():
    param.requires_grad = False

optimizer = optim.AdamW(model.classifier.parameters(), lr=1e-3, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=5)

for epoch in range(5):
    train_loss, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_loss, val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/5: train_acc={train_acc:.1f}%, val_acc={val_acc:.1f}%, lr={scheduler.get_last_lr()[0]:.6f}")

# Phase 2: Unfreeze top transformer blocks
print("\n=== Phase 2: Fine-tune top 4 transformer blocks (8 epochs) ===")
# Unfreeze last 4 blocks of ViT
blocks = list(model.visual.trunk.blocks) if hasattr(model.visual, 'trunk') else []
if blocks:
    for block in blocks[-4:]:
        for param in block.parameters():
            param.requires_grad = True
else:
    # Fallback: unfreeze all visual
    for param in model.visual.parameters():
        param.requires_grad = True

trainable = sum(p.numel() for p in model.parameters() if p.requires_grad)
print(f"  Trainable params: {trainable/1e6:.1f}M")

optimizer = optim.AdamW(filter(lambda p: p.requires_grad, model.parameters()),
                        lr=2e-5, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=8)

best_val_acc = 0
patience_counter = 0

for epoch in range(8):
    train_loss, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_loss, val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/8: train_acc={train_acc:.1f}%, val_acc={val_acc:.1f}%")

    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), "/tmp/best_sigclip_food.pth")
        patience_counter = 0
    else:
        patience_counter += 1
        if patience_counter >= 4:
            print("  Early stopping!")
            break

# Phase 3: Full fine-tune
print("\n=== Phase 3: Full fine-tune (5 epochs) ===")
for param in model.parameters():
    param.requires_grad = True

optimizer = optim.AdamW(model.parameters(), lr=5e-6, weight_decay=0.01)
scheduler = optim.lr_scheduler.CosineAnnealingLR(optimizer, T_max=5)

# Load best from phase 2
model.load_state_dict(torch.load("/tmp/best_sigclip_food.pth", weights_only=True))

for epoch in range(5):
    train_loss, train_acc = train_epoch(model, train_loader, optimizer, criterion, DEVICE)
    val_loss, val_acc = evaluate(model, val_loader, criterion, DEVICE)
    scheduler.step()
    print(f"  Epoch {epoch+1}/5: train_acc={train_acc:.1f}%, val_acc={val_acc:.1f}%")

    if val_acc > best_val_acc:
        best_val_acc = val_acc
        torch.save(model.state_dict(), "/tmp/best_sigclip_food.pth")

# Load best model
model.load_state_dict(torch.load("/tmp/best_sigclip_food.pth", weights_only=True))
print(f"\nBest validation accuracy: {best_val_acc:.1f}%")

# =====================================================
# STEP 5: Export to ONNX then TFLite
# =====================================================
print("\n=== Exporting to ONNX ===")
model.eval().cpu()
dummy_input = torch.randn(1, 3, IMG_SIZE, IMG_SIZE)
torch.onnx.export(
    model, dummy_input, OUTPUT_ONNX,
    input_names=['image'],
    output_names=['logits'],
    dynamic_axes={'image': {0: 'batch'}, 'logits': {0: 'batch'}},
    opset_version=17
)
onnx_size = os.path.getsize(OUTPUT_ONNX) / (1024*1024)
print(f"ONNX saved: {onnx_size:.1f} MB")

print("\n=== Converting ONNX to TFLite ===")
# Use onnx2tf or the tf approach
try:
    import subprocess
    subprocess.run(["pip", "install", "onnx2tf", "onnx", "sng4onnx"], check=True,
                   capture_output=True)
    subprocess.run([
        "onnx2tf", "-i", OUTPUT_ONNX, "-o", "/tmp/sigclip_tf",
        "-oiqt",  # int8 quantization
    ], check=True)
    # Find the tflite file
    tflite_files = list(Path("/tmp/sigclip_tf").glob("*.tflite"))
    if tflite_files:
        shutil.copy(str(tflite_files[0]), OUTPUT_TFLITE)
        size = os.path.getsize(OUTPUT_TFLITE) / (1024*1024)
        print(f"TFLite saved: {size:.1f} MB")
    else:
        print("WARNING: No tflite file found, saving ONNX only")
except Exception as e:
    print(f"TFLite conversion failed: {e}")
    print("You can convert manually: onnx2tf -i /tmp/food_sigclip.onnx -o /tmp/sigclip_tf -oiqt")

print(f"\nDone! {NUM_CLASSES} classes, best val accuracy: {best_val_acc:.1f}%")
