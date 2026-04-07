"""
Comprehensive ingredient + dish list for Alertgia.
Download images for all missing classes.
"""

# =====================================================
# COMPLETE INGREDIENT LIST (organized by category)
# =====================================================

INGREDIENTS = {
    # === FRUITS ===
    "apple": "fresh red apple fruit",
    "orange": "orange citrus fruit",
    "pear": "pear fruit fresh",
    "plum": "plum fruit purple",
    "apricot": "apricot fruit fresh",
    "fig": "fig fruit fresh",
    "date": "date fruit dried",
    "pomegranate": "pomegranate fruit seeds",
    "passion_fruit": "passion fruit tropical",
    "papaya": "papaya tropical fruit",
    "pineapple": "pineapple tropical fruit",
    "watermelon": "watermelon slice fruit",
    "melon": "cantaloupe melon fruit",
    "blueberry": "blueberries fruit",
    "raspberry": "raspberries fruit",
    "blackberry": "blackberries fruit",
    "cranberry": "cranberries fruit",
    "grapefruit": "grapefruit citrus fruit",
    "lime": "lime citrus green fruit",
    "tangerine": "tangerine mandarin fruit",
    "persimmon": "persimmon fruit",
    "guava": "guava tropical fruit",
    "lychee": "lychee fruit",
    "dragonfruit": "dragon fruit pitaya",
    "raisin": "raisins dried grapes",
    "prune": "prunes dried plums",

    # === VEGETABLES ===
    "potato": "potato vegetable",
    "sweet_potato": "sweet potato yam",
    "carrot": "carrots vegetable",
    "broccoli": "broccoli vegetable",
    "cauliflower": "cauliflower vegetable",
    "cabbage": "cabbage vegetable",
    "spinach": "spinach leaves vegetable",
    "kale": "kale leafy green",
    "lettuce": "lettuce salad green",
    "cucumber": "cucumber vegetable",
    "zucchini": "zucchini courgette vegetable",
    "eggplant": "eggplant aubergine vegetable",
    "bell_pepper": "bell pepper colorful",
    "chili_pepper": "chili pepper hot spicy",
    "jalapeno": "jalapeno pepper green",
    "pumpkin": "pumpkin squash",
    "butternut_squash": "butternut squash",
    "artichoke": "artichoke vegetable",
    "asparagus": "asparagus green vegetable",
    "green_pea": "green peas vegetable",
    "radish": "radish vegetable",
    "turnip": "turnip root vegetable",
    "beet": "beetroot vegetable",
    "leek": "leek vegetable",
    "scallion": "scallion green onion",
    "shallot": "shallot onion",
    "ginger": "ginger root spice",
    "turmeric": "turmeric root yellow",
    "fennel": "fennel vegetable",
    "okra": "okra vegetable",
    "corn_kernel": "corn kernels sweet",
    "edamame": "edamame soybeans pods",
    "brussels_sprout": "brussels sprouts vegetable",
    "bok_choy": "bok choy chinese cabbage",
    "watercress": "watercress green leaves",
    "arugula": "arugula rocket salad",
    "parsley": "parsley herb fresh",
    "cilantro": "cilantro coriander herb",
    "basil": "basil herb fresh",
    "mint": "mint herb fresh leaves",
    "rosemary": "rosemary herb sprig",
    "thyme": "thyme herb sprig",
    "oregano": "oregano herb dried",
    "dill": "dill herb fresh",
    "chive": "chives herb",
    "bay_leaf": "bay leaves dried",

    # === DAIRY & EGGS ===
    "egg": "chicken egg whole",
    "milk": "glass of milk dairy",
    "butter": "butter stick dairy",
    "cheese": "cheese wedge sliced",
    "mozzarella": "mozzarella cheese fresh",
    "parmesan": "parmesan cheese grated",
    "cheddar": "cheddar cheese block",
    "gouda": "gouda cheese sliced",
    "brie": "brie cheese wheel",
    "goat_cheese": "goat cheese chevre",
    "blue_cheese": "blue cheese roquefort",
    "feta": "feta cheese crumbled",
    "ricotta": "ricotta cheese",
    "cream_cheese": "cream cheese spread",
    "sour_cream": "sour cream dairy",
    "heavy_cream": "heavy cream pouring",
    "condensed_milk": "condensed milk can",
    "ice_cream_scoop": "ice cream scoop bowl",

    # === MEAT ===
    "chicken_breast": "raw chicken breast meat",
    "chicken_thigh": "chicken thigh meat",
    "chicken_wing": "raw chicken wings",
    "turkey": "turkey meat sliced",
    "duck": "duck breast meat",
    "beef_steak": "raw beef steak",
    "ground_beef": "ground beef minced meat",
    "veal": "veal meat cutlet",
    "pork_chop": "raw pork chop meat",
    "pork_belly": "pork belly meat",
    "bacon": "bacon strips meat",
    "ham": "ham sliced deli meat",
    "chorizo": "chorizo sausage spanish",
    "salami": "salami sliced meat",
    "sausage": "sausage links meat",
    "lamb_chop": "lamb chop meat",
    "lamb_leg": "lamb leg roast",
    "rabbit": "rabbit meat",
    "venison": "venison deer meat",
    "liver": "liver organ meat",
    "prosciutto": "prosciutto italian ham",
    "pancetta": "pancetta italian bacon",
    "morcilla": "morcilla blood sausage spanish",
    "jamon_iberico": "jamon iberico spanish ham",
    "lomo": "lomo embuchado spanish cured",

    # === FISH & SEAFOOD ===
    "shrimp": "raw shrimp prawns",
    "lobster": "lobster whole seafood",
    "crab": "crab whole seafood",
    "clam": "clams shellfish",
    "prawn": "prawns large shrimp",
    "langoustine": "langoustine scampi",
    "sea_bass": "sea bass fish fillet",
    "hake": "hake fish fillet",
    "trout": "trout fish whole",
    "swordfish": "swordfish steak",
    "monkfish": "monkfish fillet",
    "octopus_tentacle": "octopus tentacle cooked",
    "calamari_ring": "calamari rings fried",
    "cockle": "cockles shellfish",
    "crayfish": "crayfish freshwater",
    "sea_urchin": "sea urchin uni food",
    "eel": "eel fish food",
    "herring": "herring fish food",
    "perch": "perch fish fillet",
    "catfish": "catfish fillet food",
    "tilapia": "tilapia fish fillet",

    # === GRAINS & PASTA ===
    "white_rice": "white rice cooked bowl",
    "brown_rice": "brown rice cooked",
    "wild_rice": "wild rice cooked",
    "quinoa": "quinoa grain cooked",
    "bulgur": "bulgur wheat grain",
    "farro": "farro grain cooked",
    "millet": "millet grain cereal",
    "amaranth": "amaranth grain",
    "spaghetti_pasta": "spaghetti pasta dry",
    "penne": "penne pasta dry",
    "fusilli": "fusilli pasta spiral",
    "macaroni": "macaroni pasta elbow",
    "lasagna_sheet": "lasagna sheets pasta",
    "ravioli_pasta": "ravioli pasta filled",
    "gnocchi_pasta": "gnocchi potato pasta",
    "ramen_noodle": "ramen noodles asian",
    "udon": "udon noodles thick japanese",
    "soba": "soba noodles buckwheat",
    "rice_noodle": "rice noodles thin asian",
    "tortilla": "tortilla flatbread",
    "pita_bread": "pita bread flatbread",
    "naan": "naan bread indian",
    "baguette": "baguette french bread",
    "sourdough": "sourdough bread loaf",
    "croissant": "croissant pastry butter",
    "bagel": "bagel bread",
    "cracker": "crackers snack",
    "breadcrumb": "breadcrumbs coating",
    "panko": "panko japanese breadcrumbs",
    "cornmeal": "cornmeal polenta flour",
    "tapioca": "tapioca pearls boba",

    # === CONDIMENTS & SAUCES ===
    "ketchup": "ketchup sauce bottle",
    "mayonnaise": "mayonnaise sauce",
    "mustard_sauce": "mustard yellow sauce",
    "hot_sauce": "hot sauce chili bottle",
    "vinegar": "vinegar bottle food",
    "balsamic_vinegar": "balsamic vinegar dark",
    "tahini": "tahini sesame paste",
    "peanut_butter": "peanut butter jar spread",
    "nutella": "nutella chocolate spread",
    "jam": "jam fruit preserve jar",
    "maple_syrup": "maple syrup bottle",
    "worcestershire": "worcestershire sauce bottle",
    "fish_sauce": "fish sauce asian bottle",
    "oyster_sauce": "oyster sauce asian",
    "sriracha": "sriracha hot sauce",
    "wasabi": "wasabi green paste japanese",
    "horseradish": "horseradish sauce",
    "salsa": "salsa tomato sauce mexican",
    "guacamole_dip": "guacamole avocado dip",
    "hummus_dip": "hummus chickpea dip",
    "pesto": "pesto basil sauce green",
    "aioli": "aioli garlic mayonnaise",
    "romesco": "romesco sauce spanish",

    # === SPICES & SEASONINGS ===
    "black_pepper": "black pepper ground spice",
    "cumin": "cumin spice ground",
    "paprika": "paprika spice red powder",
    "smoked_paprika": "smoked paprika pimenton",
    "cayenne": "cayenne pepper powder",
    "nutmeg": "nutmeg spice whole",
    "cardamom": "cardamom pods spice",
    "cloves": "cloves spice dried",
    "star_anise": "star anise spice",
    "vanilla": "vanilla bean pod",
    "curry_powder": "curry powder spice",
    "garam_masala": "garam masala spice blend",
    "five_spice": "five spice chinese powder",
    "za_atar": "zaatar middle eastern spice",
    "sumac": "sumac spice red",
    "salt": "salt crystals food",
    "sea_salt": "sea salt flakes",

    # === SPANISH / MEDITERRANEAN DISHES ===
    "paella": "paella spanish rice dish",
    "fabada": "fabada asturiana bean stew",
    "tortilla_espanola": "tortilla espanola spanish potato omelette",
    "gazpacho": "gazpacho cold tomato soup spanish",
    "salmorejo": "salmorejo spanish cold soup",
    "croquetas": "croquetas spanish croquettes",
    "patatas_bravas": "patatas bravas spanish potatoes",
    "pimientos_padron": "pimientos de padron spanish peppers",
    "pulpo_gallego": "pulpo a la gallega galician octopus",
    "gambas_ajillo": "gambas al ajillo garlic shrimp spanish",
    "cochinillo": "cochinillo roast suckling pig spanish",
    "empanada_gallega": "empanada gallega spanish pie",
    "churros_chocolate": "churros with chocolate spanish",
    "flan_espanol": "flan caramel custard spanish",
    "crema_catalana": "crema catalana spanish dessert",
    "tarta_santiago": "tarta de santiago almond cake",
    "arroz_negro": "arroz negro black rice squid ink",
    "fideuà": "fideua spanish noodle paella",
    "callos": "callos tripe stew spanish",
    "pisto": "pisto manchego spanish ratatouille",
    "migas": "migas spanish fried breadcrumbs",
    "salchichon": "salchichon spanish salami",
    "manchego": "manchego cheese spanish",
    "escalivada": "escalivada roasted vegetables catalan",
    "pa_amb_tomaquet": "pa amb tomaquet bread tomato catalan",
    "calcots": "calcots grilled spring onions catalan",
    "pintxos": "pintxos basque tapas skewers",

    # === OTHER INTERNATIONAL DISHES ===
    "tacos_dish": "tacos mexican food",
    "burrito_dish": "burrito wrapped mexican",
    "quesadilla": "quesadilla cheese mexican",
    "enchilada": "enchiladas mexican dish",
    "tamale": "tamale corn husk mexican",
    "ceviche_dish": "ceviche raw fish latin",
    "arepa": "arepa corn flatbread colombian",
    "empanada": "empanada pastry filled",
    "pupusa": "pupusa salvadoran corn",
    "curry_dish": "curry indian dish bowl",
    "tikka_masala": "chicken tikka masala indian",
    "biryani": "biryani rice indian dish",
    "samosa_dish": "samosa fried pastry indian",
    "dal": "dal lentil soup indian",
    "naan_bread": "naan bread indian",
    "dim_sum": "dim sum chinese dumplings",
    "spring_roll_dish": "spring rolls asian fried",
    "fried_rice_dish": "fried rice asian wok",
    "pho_soup": "pho vietnamese noodle soup",
    "banh_mi": "banh mi vietnamese sandwich",
    "satay": "satay skewers peanut sauce",
    "rendang": "rendang beef indonesian",
    "kimchi": "kimchi korean fermented cabbage",
    "bibimbap_dish": "bibimbap korean rice bowl",
    "sushi_roll": "sushi roll japanese",
    "ramen_dish": "ramen japanese noodle soup",
    "tempura": "tempura fried battered japanese",
    "gyoza_dish": "gyoza japanese dumplings",
    "okonomiyaki": "okonomiyaki japanese pancake",
    "falafel_dish": "falafel chickpea fried balls",
    "shawarma": "shawarma meat wrap middle eastern",
    "kebab": "kebab grilled meat skewer",
    "tabbouleh": "tabbouleh salad middle eastern",
    "baklava_dish": "baklava pastry nuts syrup",
    "moussaka": "moussaka greek eggplant dish",
    "souvlaki": "souvlaki greek meat skewer",
    "pierogi": "pierogi polish dumplings",
    "borscht": "borscht beet soup eastern european",
    "schnitzel": "schnitzel breaded meat cutlet",
    "bratwurst": "bratwurst german sausage",
    "couscous_dish": "couscous north african dish",
    "tagine": "tagine moroccan stew",
    "jollof_rice": "jollof rice west african",
    "injera": "injera ethiopian flatbread",
    "poutine_dish": "poutine fries gravy cheese canadian",
}

# Allergen mapping for new ingredients
ALLERGEN_MAP = {
    # Fruits (latex cross-reactive)
    "apple": set(), "orange": set(), "pear": set(), "plum": set(),
    "apricot": set(), "fig": set(), "date": set(),
    "pomegranate": set(), "passion_fruit": set(),
    "papaya": {"Latex (food cross-reactive)"},
    "pineapple": set(), "watermelon": set(), "melon": set(),
    "blueberry": set(), "raspberry": set(), "blackberry": set(),
    "cranberry": set(), "grapefruit": set(), "lime": set(),
    "tangerine": set(), "persimmon": set(), "guava": set(),
    "lychee": set(), "dragonfruit": set(), "raisin": {"Sulfites"},
    "prune": {"Sulfites"},

    # Vegetables
    "potato": set(), "sweet_potato": set(), "carrot": set(),
    "broccoli": set(), "cauliflower": set(), "cabbage": set(),
    "spinach": set(), "kale": set(), "lettuce": set(),
    "cucumber": set(), "zucchini": set(),
    "eggplant": set(), "bell_pepper": set(),
    "chili_pepper": set(), "jalapeno": set(),
    "pumpkin": set(), "butternut_squash": set(),
    "artichoke": set(), "asparagus": set(),
    "green_pea": {"Legumes"}, "radish": set(), "turnip": set(),
    "beet": set(), "leek": set(), "scallion": set(),
    "shallot": set(), "ginger": set(), "turmeric": set(),
    "fennel": set(), "okra": set(), "corn_kernel": {"Corn"},
    "edamame": {"Soy", "Legumes"},
    "brussels_sprout": set(), "bok_choy": set(),
    "watercress": set(), "arugula": set(),
    "parsley": set(), "cilantro": set(), "basil": set(),
    "mint": set(), "rosemary": set(), "thyme": set(),
    "oregano": set(), "dill": set(), "chive": set(), "bay_leaf": set(),

    # Dairy & Eggs
    "egg": {"Eggs"}, "milk": {"Milk"}, "butter": {"Milk"},
    "cheese": {"Milk"}, "mozzarella": {"Milk"}, "parmesan": {"Milk"},
    "cheddar": {"Milk"}, "gouda": {"Milk"}, "brie": {"Milk"},
    "goat_cheese": {"Milk"}, "blue_cheese": {"Milk"}, "feta": {"Milk"},
    "ricotta": {"Milk"}, "cream_cheese": {"Milk"},
    "sour_cream": {"Milk"}, "heavy_cream": {"Milk"},
    "condensed_milk": {"Milk"}, "ice_cream_scoop": {"Milk", "Eggs"},

    # Meat
    "chicken_breast": set(), "chicken_thigh": set(), "chicken_wing": set(),
    "turkey": set(), "duck": set(), "beef_steak": set(),
    "ground_beef": set(), "veal": set(), "pork_chop": set(),
    "pork_belly": set(), "bacon": set(), "ham": set(),
    "chorizo": set(), "salami": set(), "sausage": set(),
    "lamb_chop": set(), "lamb_leg": set(), "rabbit": set(),
    "venison": set(), "liver": set(), "prosciutto": set(),
    "pancetta": set(), "morcilla": set(), "jamon_iberico": set(),
    "lomo": set(),

    # Fish & Seafood
    "shrimp": {"Shellfish"}, "lobster": {"Shellfish"},
    "crab": {"Shellfish"}, "clam": {"Shellfish", "Mollusks"},
    "prawn": {"Shellfish"}, "langoustine": {"Shellfish"},
    "sea_bass": {"Fish"}, "hake": {"Fish"}, "trout": {"Fish"},
    "swordfish": {"Fish"}, "monkfish": {"Fish"},
    "octopus_tentacle": {"Mollusks"}, "calamari_ring": {"Mollusks"},
    "cockle": {"Shellfish", "Mollusks"}, "crayfish": {"Shellfish"},
    "sea_urchin": {"Shellfish"}, "eel": {"Fish"},
    "herring": {"Fish"}, "perch": {"Fish"}, "catfish": {"Fish"},
    "tilapia": {"Fish"},

    # Grains & Pasta
    "white_rice": set(), "brown_rice": set(), "wild_rice": set(),
    "quinoa": set(), "bulgur": {"Wheat", "Gluten"},
    "farro": {"Wheat", "Gluten"}, "millet": set(), "amaranth": set(),
    "spaghetti_pasta": {"Wheat", "Gluten", "Eggs"},
    "penne": {"Wheat", "Gluten"}, "fusilli": {"Wheat", "Gluten"},
    "macaroni": {"Wheat", "Gluten"},
    "lasagna_sheet": {"Wheat", "Gluten", "Eggs"},
    "ravioli_pasta": {"Wheat", "Gluten", "Eggs"},
    "gnocchi_pasta": {"Wheat", "Gluten", "Eggs"},
    "ramen_noodle": {"Wheat", "Gluten", "Eggs"},
    "udon": {"Wheat", "Gluten"}, "soba": {"Buckwheat"},
    "rice_noodle": set(), "tortilla": {"Wheat", "Gluten"},
    "pita_bread": {"Wheat", "Gluten"},
    "naan": {"Wheat", "Gluten", "Milk"},
    "baguette": {"Wheat", "Gluten"},
    "sourdough": {"Wheat", "Gluten"},
    "croissant": {"Wheat", "Gluten", "Milk", "Eggs"},
    "bagel": {"Wheat", "Gluten", "Sesame"},
    "cracker": {"Wheat", "Gluten"},
    "breadcrumb": {"Wheat", "Gluten"},
    "panko": {"Wheat", "Gluten"},
    "cornmeal": {"Corn"}, "tapioca": set(),

    # Condiments
    "ketchup": set(), "mayonnaise": {"Eggs"},
    "mustard_sauce": {"Mustard"},
    "hot_sauce": set(), "vinegar": {"Sulfites"},
    "balsamic_vinegar": {"Sulfites"},
    "tahini": {"Sesame"}, "peanut_butter": {"Peanuts"},
    "nutella": {"Tree Nuts", "Milk"},
    "jam": set(), "maple_syrup": set(),
    "worcestershire": {"Fish"}, "fish_sauce": {"Fish"},
    "oyster_sauce": {"Shellfish"}, "sriracha": set(),
    "wasabi": set(), "horseradish": set(), "salsa": set(),
    "guacamole_dip": set(), "hummus_dip": {"Sesame", "Chickpeas"},
    "pesto": {"Tree Nuts", "Milk"},
    "aioli": {"Eggs"}, "romesco": {"Tree Nuts"},

    # Spices
    "black_pepper": set(), "cumin": set(), "paprika": set(),
    "smoked_paprika": set(), "cayenne": set(), "nutmeg": set(),
    "cardamom": set(), "cloves": set(), "star_anise": set(),
    "vanilla": set(), "curry_powder": {"Mustard"},
    "garam_masala": set(), "five_spice": set(),
    "za_atar": {"Sesame"}, "sumac": set(),
    "salt": set(), "sea_salt": set(),

    # Spanish dishes
    "paella": {"Shellfish", "Fish"},
    "fabada": {"Legumes"},
    "tortilla_espanola": {"Eggs"},
    "gazpacho": set(), "salmorejo": {"Wheat", "Gluten"},
    "croquetas": {"Wheat", "Gluten", "Milk", "Eggs"},
    "patatas_bravas": set(),
    "pimientos_padron": set(),
    "pulpo_gallego": {"Mollusks"},
    "gambas_ajillo": {"Shellfish"},
    "cochinillo": set(),
    "empanada_gallega": {"Wheat", "Gluten"},
    "churros_chocolate": {"Wheat", "Gluten", "Milk"},
    "flan_espanol": {"Eggs", "Milk"},
    "crema_catalana": {"Eggs", "Milk"},
    "tarta_santiago": {"Tree Nuts", "Eggs"},
    "arroz_negro": {"Shellfish", "Fish"},
    "fideuà": {"Wheat", "Gluten", "Shellfish"},
    "callos": set(),
    "pisto": set(), "migas": {"Wheat", "Gluten"},
    "salchichon": set(), "manchego": {"Milk"},
    "escalivada": set(), "pa_amb_tomaquet": {"Wheat", "Gluten"},
    "calcots": set(), "pintxos": set(),

    # International dishes
    "tacos_dish": {"Corn"}, "burrito_dish": {"Wheat", "Gluten"},
    "quesadilla": {"Milk", "Wheat", "Gluten"},
    "enchilada": {"Corn", "Milk"},
    "tamale": {"Corn"}, "ceviche_dish": {"Fish"},
    "arepa": {"Corn"}, "empanada": {"Wheat", "Gluten"},
    "pupusa": {"Corn"}, "curry_dish": set(),
    "tikka_masala": {"Milk"}, "biryani": set(),
    "samosa_dish": {"Wheat", "Gluten"},
    "dal": {"Legumes", "Lentils"},
    "naan_bread": {"Wheat", "Gluten", "Milk"},
    "dim_sum": {"Wheat", "Gluten", "Soy"},
    "spring_roll_dish": {"Wheat", "Gluten"},
    "fried_rice_dish": {"Eggs", "Soy"},
    "pho_soup": {"Fish"}, "banh_mi": {"Wheat", "Gluten"},
    "satay": {"Peanuts"}, "rendang": set(),
    "kimchi": set(), "bibimbap_dish": {"Eggs", "Sesame"},
    "sushi_roll": {"Fish", "Soy", "Sesame"},
    "ramen_dish": {"Wheat", "Gluten", "Eggs", "Soy"},
    "tempura": {"Wheat", "Gluten", "Eggs"},
    "gyoza_dish": {"Wheat", "Gluten", "Soy"},
    "okonomiyaki": {"Wheat", "Gluten", "Eggs"},
    "falafel_dish": {"Chickpeas", "Legumes"},
    "shawarma": set(), "kebab": set(),
    "tabbouleh": {"Wheat", "Gluten"},
    "baklava_dish": {"Tree Nuts", "Wheat", "Gluten"},
    "moussaka": {"Milk", "Eggs"},
    "souvlaki": set(), "pierogi": {"Wheat", "Gluten", "Eggs"},
    "borscht": set(), "schnitzel": {"Wheat", "Gluten", "Eggs"},
    "bratwurst": set(),
    "couscous_dish": {"Wheat", "Gluten"},
    "tagine": set(), "jollof_rice": set(),
    "injera": set(),
    "poutine_dish": {"Milk"},
}

if __name__ == "__main__":
    import os
    import shutil

    # Count existing vs new
    existing_dir = "/Users/xai/alertgia/training_data/ingredients"
    existing = set(os.listdir(existing_dir)) if os.path.exists(existing_dir) else set()

    new_classes = set(INGREDIENTS.keys()) - existing
    print(f"Total ingredients/dishes: {len(INGREDIENTS)}")
    print(f"Already have images: {len(existing & set(INGREDIENTS.keys()))}")
    print(f"Need to download: {len(new_classes)}")

    # Download missing
    from bing_image_downloader import downloader

    for class_name in sorted(new_classes):
        query = INGREDIENTS[class_name]
        output_dir = os.path.join(existing_dir, class_name)
        if os.path.exists(output_dir) and len([f for f in os.listdir(output_dir) if f.lower().endswith(('.jpg','.jpeg'))]) >= 5:
            print(f"    SKIP {class_name} — already have enough images")
            continue

        print(f"  Downloading {class_name} ({query})...")
        try:
            downloader.download(query, limit=20, output_dir="/tmp/bing_temp",
                              adult_filter_off=False, force_replace=False,
                              timeout=30, verbose=False)
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
                shutil.rmtree(query_dir, ignore_errors=True)
                print(f"    -> {moved} images")
        except Exception as e:
            print(f"    -> ERROR: {e}")

    # Convert all to clean JPEG
    print("\n=== Converting to JPEG ===")
    from PIL import Image
    fixed = 0
    for root, dirs, files in os.walk(existing_dir):
        for f in files:
            fp = os.path.join(root, f)
            try:
                img = Image.open(fp).convert('RGB')
                new_fp = os.path.splitext(fp)[0] + '.jpg'
                img.save(new_fp, 'JPEG', quality=90)
                if new_fp != fp: os.remove(fp)
                fixed += 1
            except:
                os.remove(fp)
    print(f"Converted {fixed} images")

    # Final count
    total = 0
    classes = 0
    for d in sorted(os.listdir(existing_dir)):
        dp = os.path.join(existing_dir, d)
        if os.path.isdir(dp):
            n = len([f for f in os.listdir(dp) if f.endswith('.jpg')])
            if n >= 3:
                classes += 1
                total += n
    print(f"\nFinal: {classes} classes, {total} images")
    print("Done!")
