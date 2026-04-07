"""
Download ingredient images from multiple sources and organize into training directory.
Then train EfficientNetV2B2 on Food-101 + real ingredient images.
"""
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

INGREDIENTS_DIR = "/tmp/ingredient_dataset"
IMAGES_PER_CLASS = 150  # target per class from Bing

# Classes we need that Fruits-360 won't cover well
BING_DOWNLOAD_CLASSES = {
    # Nuts (specific views)
    "peanut": "raw peanuts food close up",
    "almond": "raw almonds food close up",
    "walnut": "walnuts food close up",
    "cashew": "cashew nuts food",
    "pistachio": "pistachio nuts food",
    "hazelnut": "hazelnuts food close up",
    "pecan": "pecan nuts food",
    "macadamia": "macadamia nuts food",
    "brazil_nut": "brazil nuts food close up",
    "pine_nut": "pine nuts food close up",
    # Shellfish
    "shrimp": "raw shrimp food",
    "crab": "crab seafood food",
    "lobster": "lobster seafood food",
    "oyster": "oysters food plate",
    "clam": "clams seafood food",
    "mussel": "mussels seafood food",
    "scallop": "scallops seafood food",
    "squid": "squid calamari food",
    "octopus": "octopus food",
    # Dairy & eggs
    "egg": "chicken egg food",
    "milk": "glass of milk",
    "butter": "butter food",
    "cheese": "cheese food slices",
    "yogurt": "yogurt food cup",
    "cream": "cream food",
    "whey": "whey protein powder",
    "ghee": "ghee clarified butter",
    # Wheat/gluten
    "bread": "bread loaf food",
    "wheat": "wheat grain kernels",
    "flour": "wheat flour food",
    "pasta": "dry pasta food",
    "noodle": "noodles food",
    "seitan": "seitan wheat gluten food",
    "couscous": "couscous food",
    "barley": "barley grain food",
    # Soy
    "soybean": "soybeans food",
    "tofu": "tofu food block",
    "tempeh": "tempeh food",
    "soy_sauce": "soy sauce bottle food",
    "miso": "miso paste food",
    # Fish
    "salmon": "raw salmon fish food",
    "tuna": "tuna fish food",
    "sardine": "sardines fish food",
    "anchovy": "anchovies food",
    "cod": "cod fish food",
    "mackerel": "mackerel fish food",
    # Legumes
    "lentil": "lentils food",
    "chickpea": "chickpeas garbanzo food",
    "lupin": "lupin beans food",
    "green_bean": "green beans food",
    "kidney_bean": "kidney beans food",
    # Seeds
    "sesame": "sesame seeds food",
    "mustard_seed": "mustard seeds food",
    "poppy_seed": "poppy seeds food",
    "sunflower_seed": "sunflower seeds food",
    "flaxseed": "flaxseeds food",
    "chia_seed": "chia seeds food",
    # Grains
    "corn": "corn on cob food",
    "rice": "rice grains food",
    "buckwheat": "buckwheat grains food",
    "oat": "oats oatmeal food",
    # Vegetables
    "celery": "celery stalks food",
    "mustard_greens": "mustard greens vegetable",
    "mushroom": "mushrooms food",
    "tomato": "tomatoes food",
    "onion": "onion food",
    "garlic": "garlic cloves food",
    "pepper": "bell pepper food",
    "avocado": "avocado food",
    # Fruits
    "strawberry": "strawberries food",
    "kiwi": "kiwi fruit food",
    "grape": "grapes food",
    "lemon": "lemons food",
    "coconut": "coconut food",
    "mango": "mango fruit food",
    "banana": "banana fruit food",
    "peach": "peach fruit food",
    "cherry": "cherries food",
    # Other
    "chocolate": "chocolate bar food",
    "honey": "honey jar food",
    "sugar": "sugar food",
    "olive_oil": "olive oil bottle food",
    "gelatin": "gelatin sheets food",
    "cinnamon": "cinnamon sticks food",
    "saffron": "saffron spice food",
    # Meats
    "pork": "raw pork meat food",
    "beef": "raw beef meat food",
    "chicken_meat": "raw chicken meat food",
    "lamb": "raw lamb meat food",
}

def download_bing_images():
    """Download ingredient images from Bing."""
    from bing_image_downloader import downloader

    for class_name, query in BING_DOWNLOAD_CLASSES.items():
        output_dir = os.path.join(INGREDIENTS_DIR, class_name)
        if os.path.exists(output_dir) and len(os.listdir(output_dir)) >= IMAGES_PER_CLASS // 2:
            print(f"  Skipping {class_name} — already have {len(os.listdir(output_dir))} images")
            continue

        print(f"  Downloading {class_name}: '{query}'...")
        try:
            downloader.download(
                query,
                limit=IMAGES_PER_CLASS,
                output_dir=INGREDIENTS_DIR,
                adult_filter_off=False,
                force_replace=False,
                timeout=30,
                verbose=False
            )
            # bing_image_downloader saves to a subfolder named after the query
            query_dir = os.path.join(INGREDIENTS_DIR, query)
            if os.path.exists(query_dir) and query_dir != output_dir:
                os.makedirs(output_dir, exist_ok=True)
                for f in os.listdir(query_dir):
                    src = os.path.join(query_dir, f)
                    dst = os.path.join(output_dir, f)
                    if os.path.isfile(src):
                        shutil.move(src, dst)
                shutil.rmtree(query_dir, ignore_errors=True)
        except Exception as e:
            print(f"    Error downloading {class_name}: {e}")

def count_images():
    """Count images per class."""
    total = 0
    classes = 0
    for class_name in sorted(os.listdir(INGREDIENTS_DIR)):
        class_dir = os.path.join(INGREDIENTS_DIR, class_name)
        if os.path.isdir(class_dir):
            count = len([f for f in os.listdir(class_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png', '.webp'))])
            if count > 0:
                classes += 1
                total += count
                print(f"  {class_name}: {count} images")
    print(f"\nTotal: {classes} classes, {total} images")
    return classes, total

if __name__ == "__main__":
    os.makedirs(INGREDIENTS_DIR, exist_ok=True)

    print("=== Downloading ingredient images from Bing ===")
    download_bing_images()

    print("\n=== Image count per class ===")
    count_images()

    print("\nDone! Images ready at:", INGREDIENTS_DIR)
