"""Re-download ingredient classes that have 0 images."""
import os
from bing_image_downloader import downloader

INGREDIENTS_DIR = "/tmp/ingredient_dataset"
IMAGES_PER_CLASS = 150

CLASSES_TO_DOWNLOAD = {
    "anchovy": "anchovies fish food close up",
    "avocado": "avocado fruit cut half food",
    "banana": "banana fruit yellow food",
    "barley": "barley grain cereal food",
    "beef": "raw beef steak meat",
    "bread": "bread loaf sliced food",
    "buckwheat": "buckwheat grain food",
    "celery": "celery sticks vegetable food",
    "cherry": "fresh cherries fruit food",
    "chia_seed": "chia seeds superfood",
    "chicken_meat": "raw chicken breast meat",
    "chickpea": "chickpeas garbanzo beans food",
    "chocolate": "chocolate bar pieces food",
    "cinnamon": "cinnamon sticks spice food",
    "coconut": "coconut tropical fruit food",
    "cod": "cod fish fillet food",
    "corn": "corn cob kernels food",
    "couscous": "couscous grain dish food",
    "cream": "heavy cream dairy food",
    "flaxseed": "flaxseeds linseed food",
    "garlic": "garlic bulb cloves food",
    "gelatin": "gelatin sheets powder food",
    "ghee": "ghee clarified butter jar",
    "grape": "grapes bunch fruit food",
    "green_bean": "green beans string beans food",
    "honey": "honey jar golden food",
    "kidney_bean": "red kidney beans food",
    "kiwi": "kiwi fruit green food",
    "lamb": "raw lamb chop meat food",
    "lemon": "fresh lemons citrus food",
    "lentil": "lentils legume dried food",
    "lupin": "lupin beans lupini food",
    "mackerel": "mackerel fish food",
    "mango": "mango tropical fruit food",
    "miso": "miso paste japanese food",
    "mushroom": "fresh mushrooms food",
    "mustard_greens": "mustard greens leafy vegetable",
    "mustard_seed": "mustard seeds yellow food",
    "noodle": "noodles asian food",
    "oat": "oats oatmeal bowl food",
    "olive_oil": "olive oil glass bottle food",
    "onion": "onion vegetable food",
    "pasta": "dry pasta spaghetti food",
    "peach": "fresh peach fruit food",
    "pepper": "bell pepper colorful food",
    "poppy_seed": "poppy seeds food baking",
    "pork": "raw pork chop meat food",
    "rice": "white rice grain food",
    "saffron": "saffron threads spice food",
    "salmon": "raw salmon fillet fish food",
    "sardine": "sardines small fish food",
    "seitan": "seitan wheat meat food",
    "sesame": "sesame seeds food",
    "soy_sauce": "soy sauce asian condiment food",
    "soybean": "soybeans edamame food",
    "strawberry": "fresh strawberries fruit food",
    "sugar": "white sugar granulated food",
    "sunflower_seed": "sunflower seeds snack food",
    "tempeh": "tempeh fermented soy food",
    "tofu": "tofu block soy food",
    "tomato": "fresh tomatoes red food",
    "tuna": "tuna fish steak food",
    "wheat": "wheat grains kernels food",
    "whey": "whey protein supplement food",
    "yogurt": "yogurt cup dairy food",
}

for class_name, query in CLASSES_TO_DOWNLOAD.items():
    output_dir = os.path.join(INGREDIENTS_DIR, class_name)
    existing = len([f for f in os.listdir(output_dir) if os.path.isfile(os.path.join(output_dir, f))]) if os.path.exists(output_dir) else 0

    if existing >= 50:
        print(f"SKIP {class_name}: already has {existing} images")
        continue

    print(f"Downloading {class_name} ({query})...")
    try:
        downloader.download(
            query,
            limit=IMAGES_PER_CLASS,
            output_dir="/tmp/bing_temp",
            adult_filter_off=False,
            force_replace=False,
            timeout=30,
            verbose=False
        )
        # Move from query-named folder to class folder
        query_dir = os.path.join("/tmp/bing_temp", query)
        if os.path.exists(query_dir):
            os.makedirs(output_dir, exist_ok=True)
            moved = 0
            for f in os.listdir(query_dir):
                src = os.path.join(query_dir, f)
                dst = os.path.join(output_dir, f)
                if os.path.isfile(src):
                    os.rename(src, dst)
                    moved += 1
            print(f"  -> {moved} images saved to {class_name}/")
            import shutil
            shutil.rmtree(query_dir, ignore_errors=True)
        else:
            print(f"  -> WARNING: no output folder found")
    except Exception as e:
        print(f"  -> ERROR: {e}")

# Final count
print("\n=== Final count ===")
total = 0
classes = 0
for d in sorted(os.listdir(INGREDIENTS_DIR)):
    dp = os.path.join(INGREDIENTS_DIR, d)
    if os.path.isdir(dp):
        n = len([f for f in os.listdir(dp) if os.path.isfile(os.path.join(dp, f))])
        if n > 0:
            classes += 1
            total += n
print(f"{classes} classes, {total} total images")
print("Done!")
