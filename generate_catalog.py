"""Generate CSV and PDF catalog of all Alertgia classes in Spanish."""
import csv
from fpdf import FPDF

# Complete catalog: (english_key, spanish_name, spanish_allergens, category)
CATALOG = [
    # === FOOD-101 DISHES ===
    ("apple_pie", "Tarta de manzana", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("baby_back_ribs", "Costillas de cerdo", "Soja", "Plato internacional"),
    ("baklava", "Baklava", "Frutos secos, Trigo, Gluten, Huevos", "Plato internacional"),
    ("beef_carpaccio", "Carpaccio de ternera", "Huevos", "Plato internacional"),
    ("beef_tartare", "Steak tartar", "Huevos", "Plato internacional"),
    ("beet_salad", "Ensalada de remolacha", "Frutos secos", "Plato internacional"),
    ("beignets", "Buñuelos", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("bibimbap", "Bibimbap", "Huevos, Soja, Sésamo", "Plato internacional"),
    ("bread_pudding", "Pudín de pan", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("breakfast_burrito", "Burrito de desayuno", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("bruschetta", "Bruschetta", "Trigo, Gluten", "Plato internacional"),
    ("caesar_salad", "Ensalada César", "Huevos, Pescado, Gluten, Leche", "Plato internacional"),
    ("cannoli", "Cannoli", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("caprese_salad", "Ensalada caprese", "Leche", "Plato internacional"),
    ("carrot_cake", "Tarta de zanahoria", "Trigo, Gluten, Huevos, Leche, Frutos secos", "Plato internacional"),
    ("ceviche", "Ceviche", "Pescado, Marisco", "Plato internacional"),
    ("cheesecake", "Tarta de queso", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("cheese_plate", "Tabla de quesos", "Leche", "Plato internacional"),
    ("chicken_curry", "Curry de pollo", "Leche", "Plato internacional"),
    ("chicken_quesadilla", "Quesadilla de pollo", "Trigo, Gluten, Leche", "Plato internacional"),
    ("chicken_wings", "Alitas de pollo", "Trigo, Gluten, Huevos, Soja", "Plato internacional"),
    ("chocolate_cake", "Tarta de chocolate", "Trigo, Gluten, Huevos, Leche, Soja", "Plato internacional"),
    ("chocolate_mousse", "Mousse de chocolate", "Huevos, Leche, Soja", "Plato internacional"),
    ("churros", "Churros", "Trigo, Gluten, Huevos, Leche", "Plato español"),
    ("clam_chowder", "Sopa de almejas", "Marisco, Leche, Trigo, Gluten", "Plato internacional"),
    ("club_sandwich", "Sándwich club", "Trigo, Gluten, Huevos", "Plato internacional"),
    ("crab_cakes", "Pasteles de cangrejo", "Marisco, Trigo, Gluten, Huevos", "Plato internacional"),
    ("creme_brulee", "Crème brûlée", "Huevos, Leche", "Plato internacional"),
    ("croque_madame", "Croque madame", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("cup_cakes", "Cupcakes", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("deviled_eggs", "Huevos rellenos", "Huevos", "Plato internacional"),
    ("donuts", "Donuts", "Trigo, Gluten, Huevos, Leche, Soja", "Plato internacional"),
    ("dumplings", "Dumplings", "Trigo, Gluten, Soja, Sésamo", "Plato internacional"),
    ("edamame", "Edamame", "Soja", "Plato internacional"),
    ("eggs_benedict", "Huevos benedictinos", "Huevos, Trigo, Gluten, Leche", "Plato internacional"),
    ("escargots", "Caracoles", "Leche, Moluscos", "Plato internacional"),
    ("falafel", "Falafel", "Trigo, Gluten, Sésamo, Garbanzos, Legumbres", "Plato internacional"),
    ("filet_mignon", "Filete mignon", "", "Plato internacional"),
    ("fish_and_chips", "Fish and chips", "Pescado, Trigo, Gluten, Huevos", "Plato internacional"),
    ("foie_gras", "Foie gras", "", "Plato internacional"),
    ("french_fries", "Patatas fritas", "Gluten", "Plato internacional"),
    ("french_onion_soup", "Sopa de cebolla", "Trigo, Gluten, Leche", "Plato internacional"),
    ("french_toast", "Torrijas", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("fried_calamari", "Calamares fritos", "Marisco, Trigo, Gluten, Huevos", "Plato internacional"),
    ("fried_rice", "Arroz frito", "Huevos, Soja", "Plato internacional"),
    ("frozen_yogurt", "Yogur helado", "Leche", "Plato internacional"),
    ("garlic_bread", "Pan de ajo", "Trigo, Gluten, Leche", "Plato internacional"),
    ("gnocchi", "Ñoquis", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("greek_salad", "Ensalada griega", "Leche", "Plato internacional"),
    ("grilled_cheese_sandwich", "Sándwich de queso", "Trigo, Gluten, Leche", "Plato internacional"),
    ("grilled_salmon", "Salmón a la plancha", "Pescado", "Plato internacional"),
    ("guacamole", "Guacamole", "", "Plato internacional"),
    ("gyoza", "Gyoza", "Trigo, Gluten, Soja, Sésamo", "Plato internacional"),
    ("hamburger", "Hamburguesa", "Trigo, Gluten, Huevos, Leche, Sésamo", "Plato internacional"),
    ("hot_and_sour_soup", "Sopa agripicante", "Soja, Huevos", "Plato internacional"),
    ("hot_dog", "Perrito caliente", "Trigo, Gluten, Leche", "Plato internacional"),
    ("huevos_rancheros", "Huevos rancheros", "Huevos, Leche", "Plato internacional"),
    ("hummus", "Hummus", "Sésamo, Garbanzos", "Plato internacional"),
    ("ice_cream", "Helado", "Leche, Huevos, Soja", "Plato internacional"),
    ("lasagna", "Lasaña", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("lobster_bisque", "Bisque de langosta", "Marisco, Leche", "Plato internacional"),
    ("lobster_roll_sandwich", "Sándwich de langosta", "Marisco, Trigo, Gluten, Leche", "Plato internacional"),
    ("macaroni_and_cheese", "Macarrones con queso", "Trigo, Gluten, Leche, Huevos", "Plato internacional"),
    ("macarons", "Macarons", "Huevos, Frutos secos, Leche", "Plato internacional"),
    ("miso_soup", "Sopa de miso", "Soja, Pescado", "Plato internacional"),
    ("mussels", "Mejillones", "Marisco, Moluscos, Leche", "Plato internacional"),
    ("nachos", "Nachos", "Leche, Maíz", "Plato internacional"),
    ("omelette", "Tortilla francesa", "Huevos, Leche", "Plato internacional"),
    ("onion_rings", "Aros de cebolla", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("oysters", "Ostras", "Marisco, Moluscos", "Plato internacional"),
    ("pad_thai", "Pad thai", "Cacahuetes, Marisco, Huevos, Pescado, Soja", "Plato internacional"),
    ("paella", "Paella", "Marisco, Pescado", "Plato español"),
    ("pancakes", "Tortitas", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("panna_cotta", "Panna cotta", "Leche", "Plato internacional"),
    ("peking_duck", "Pato pekín", "Trigo, Gluten, Soja", "Plato internacional"),
    ("pho", "Pho", "Soja, Pescado", "Plato internacional"),
    ("pizza", "Pizza", "Trigo, Gluten, Leche, Huevos", "Plato internacional"),
    ("pork_chop", "Chuleta de cerdo", "", "Plato internacional"),
    ("poutine", "Poutine", "Leche, Gluten, Trigo", "Plato internacional"),
    ("prime_rib", "Costillar", "", "Plato internacional"),
    ("pulled_pork_sandwich", "Sándwich de cerdo", "Trigo, Gluten", "Plato internacional"),
    ("ramen", "Ramen", "Trigo, Gluten, Huevos, Soja", "Plato internacional"),
    ("ravioli", "Ravioli", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("red_velvet_cake", "Tarta red velvet", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("risotto", "Risotto", "Leche", "Plato internacional"),
    ("samosa", "Samosa", "Trigo, Gluten", "Plato internacional"),
    ("sashimi", "Sashimi", "Pescado, Soja", "Plato internacional"),
    ("scallops", "Vieiras", "Marisco, Moluscos", "Plato internacional"),
    ("seaweed_salad", "Ensalada de algas", "Soja, Sésamo", "Plato internacional"),
    ("shrimp_and_grits", "Gambas con sémola", "Marisco, Leche, Maíz", "Plato internacional"),
    ("spaghetti_bolognese", "Espaguetis boloñesa", "Trigo, Gluten", "Plato internacional"),
    ("spaghetti_carbonara", "Espaguetis carbonara", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("spring_rolls", "Rollitos de primavera", "Trigo, Gluten, Soja, Marisco", "Plato internacional"),
    ("steak", "Filete", "", "Plato internacional"),
    ("strawberry_shortcake", "Tarta de fresas", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),
    ("sushi", "Sushi", "Pescado, Marisco, Soja, Sésamo", "Plato internacional"),
    ("tacos", "Tacos", "Maíz, Leche", "Plato internacional"),
    ("takoyaki", "Takoyaki", "Trigo, Gluten, Huevos, Marisco", "Plato internacional"),
    ("tiramisu", "Tiramisú", "Huevos, Leche, Trigo, Gluten", "Plato internacional"),
    ("tuna_tartare", "Tartar de atún", "Pescado, Soja, Sésamo", "Plato internacional"),
    ("waffles", "Gofres", "Trigo, Gluten, Huevos, Leche", "Plato internacional"),

    # === SPANISH DISHES ===
    ("tortilla_espanola", "Tortilla española", "Huevos", "Plato español"),
    ("fabada", "Fabada asturiana", "Legumbres", "Plato español"),
    ("gazpacho", "Gazpacho", "", "Plato español"),
    ("salmorejo", "Salmorejo", "Trigo, Gluten", "Plato español"),
    ("croquetas", "Croquetas", "Huevos, Gluten, Leche, Trigo", "Plato español"),
    ("patatas_bravas", "Patatas bravas", "", "Plato español"),
    ("pimientos_padron", "Pimientos de padrón", "", "Plato español"),
    ("pulpo_gallego", "Pulpo a la gallega", "Moluscos", "Plato español"),
    ("gambas_ajillo", "Gambas al ajillo", "Marisco", "Plato español"),
    ("cochinillo", "Cochinillo asado", "", "Plato español"),
    ("empanada_gallega", "Empanada gallega", "Trigo, Gluten", "Plato español"),
    ("churros_chocolate", "Churros con chocolate", "Trigo, Gluten, Leche", "Plato español"),
    ("flan_espanol", "Flan", "Huevos, Leche", "Plato español"),
    ("crema_catalana", "Crema catalana", "Huevos, Leche", "Plato español"),
    ("tarta_santiago", "Tarta de Santiago", "Huevos, Frutos secos", "Plato español"),
    ("arroz_negro", "Arroz negro", "Pescado, Marisco", "Plato español"),
    ("fideuà", "Fideuá", "Gluten, Marisco, Trigo", "Plato español"),
    ("callos", "Callos", "", "Plato español"),
    ("pisto", "Pisto manchego", "", "Plato español"),
    ("migas", "Migas", "Trigo, Gluten", "Plato español"),
    ("salchichon", "Salchichón", "", "Plato español"),
    ("manchego", "Queso manchego", "Leche", "Plato español"),
    ("escalivada", "Escalivada", "", "Plato español"),
    ("pa_amb_tomaquet", "Pa amb tomàquet", "Trigo, Gluten", "Plato español"),
    ("calcots", "Calçots", "", "Plato español"),
    ("pintxos", "Pintxos", "", "Plato español"),
    ("jamon_iberico", "Jamón ibérico", "", "Plato español"),
    ("chorizo", "Chorizo", "", "Plato español"),
    ("morcilla", "Morcilla", "", "Plato español"),
    ("lomo", "Lomo embuchado", "", "Plato español"),

    # === INTERNATIONAL DISHES ===
    ("tacos_dish", "Tacos", "Maíz", "Plato internacional"),
    ("burrito_dish", "Burrito", "Trigo, Gluten", "Plato internacional"),
    ("quesadilla", "Quesadilla", "Trigo, Gluten, Leche", "Plato internacional"),
    ("enchilada", "Enchilada", "Maíz, Leche", "Plato internacional"),
    ("tamale", "Tamal", "Maíz", "Plato internacional"),
    ("ceviche_dish", "Ceviche", "Pescado", "Plato internacional"),
    ("arepa", "Arepa", "Maíz", "Plato internacional"),
    ("empanada", "Empanada", "Trigo, Gluten", "Plato internacional"),
    ("pupusa", "Pupusa", "Maíz", "Plato internacional"),
    ("curry_dish", "Curry", "", "Plato internacional"),
    ("tikka_masala", "Tikka masala", "Leche", "Plato internacional"),
    ("biryani", "Biryani", "", "Plato internacional"),
    ("samosa_dish", "Samosa", "Trigo, Gluten", "Plato internacional"),
    ("dal", "Dal de lentejas", "Legumbres, Lentejas", "Plato internacional"),
    ("naan_bread", "Pan naan", "Trigo, Gluten, Leche", "Plato internacional"),
    ("dim_sum", "Dim sum", "Trigo, Gluten, Soja", "Plato internacional"),
    ("spring_roll_dish", "Rollitos de primavera", "Trigo, Gluten", "Plato internacional"),
    ("fried_rice_dish", "Arroz frito", "Huevos, Soja", "Plato internacional"),
    ("pho_soup", "Pho", "Pescado", "Plato internacional"),
    ("banh_mi", "Bánh mì", "Trigo, Gluten", "Plato internacional"),
    ("satay", "Satay", "Cacahuetes", "Plato internacional"),
    ("rendang", "Rendang", "", "Plato internacional"),
    ("kimchi", "Kimchi", "", "Plato internacional"),
    ("bibimbap_dish", "Bibimbap", "Huevos, Sésamo", "Plato internacional"),
    ("sushi_roll", "Maki sushi", "Pescado, Soja, Sésamo", "Plato internacional"),
    ("ramen_dish", "Ramen", "Trigo, Gluten, Huevos, Soja", "Plato internacional"),
    ("tempura", "Tempura", "Trigo, Gluten, Huevos", "Plato internacional"),
    ("gyoza_dish", "Gyoza", "Trigo, Gluten, Soja", "Plato internacional"),
    ("okonomiyaki", "Okonomiyaki", "Trigo, Gluten, Huevos", "Plato internacional"),
    ("falafel_dish", "Falafel", "Garbanzos, Legumbres", "Plato internacional"),
    ("shawarma", "Shawarma", "", "Plato internacional"),
    ("kebab", "Kebab", "", "Plato internacional"),
    ("tabbouleh", "Tabulé", "Trigo, Gluten", "Plato internacional"),
    ("baklava_dish", "Baklava", "Frutos secos, Trigo, Gluten", "Plato internacional"),
    ("moussaka", "Musaka", "Huevos, Leche", "Plato internacional"),
    ("souvlaki", "Souvlaki", "", "Plato internacional"),
    ("pierogi", "Pierogi", "Trigo, Gluten, Huevos", "Plato internacional"),
    ("borscht", "Borscht", "", "Plato internacional"),
    ("schnitzel", "Schnitzel", "Trigo, Gluten, Huevos", "Plato internacional"),
    ("bratwurst", "Bratwurst", "", "Plato internacional"),
    ("couscous_dish", "Cuscús", "Trigo, Gluten", "Plato internacional"),
    ("tagine", "Tajín", "", "Plato internacional"),
    ("jollof_rice", "Arroz jollof", "", "Plato internacional"),
    ("injera", "Injera", "", "Plato internacional"),
    ("poutine_dish", "Poutine", "Leche", "Plato internacional"),

    # === NUTS ===
    ("peanut", "Cacahuete", "Cacahuetes", "Ingrediente - Frutos secos"),
    ("almond", "Almendra", "Frutos secos", "Ingrediente - Frutos secos"),
    ("walnut", "Nuez", "Frutos secos", "Ingrediente - Frutos secos"),
    ("cashew", "Anacardo", "Frutos secos", "Ingrediente - Frutos secos"),
    ("pistachio", "Pistacho", "Frutos secos", "Ingrediente - Frutos secos"),
    ("hazelnut", "Avellana", "Frutos secos", "Ingrediente - Frutos secos"),
    ("pecan", "Nuez pecana", "Frutos secos", "Ingrediente - Frutos secos"),
    ("macadamia", "Macadamia", "Frutos secos", "Ingrediente - Frutos secos"),
    ("brazil_nut", "Nuez de Brasil", "Frutos secos", "Ingrediente - Frutos secos"),
    ("pine_nut", "Piñón", "Frutos secos", "Ingrediente - Frutos secos"),
    ("coconut", "Coco", "Frutos secos", "Ingrediente - Frutos secos"),

    # === DAIRY & EGGS ===
    ("egg", "Huevo", "Huevos", "Ingrediente - Lácteos y huevos"),
    ("milk", "Leche", "Leche", "Ingrediente - Lácteos y huevos"),
    ("butter", "Mantequilla", "Leche", "Ingrediente - Lácteos y huevos"),
    ("cheese", "Queso", "Leche", "Ingrediente - Lácteos y huevos"),
    ("mozzarella", "Mozzarella", "Leche", "Ingrediente - Lácteos y huevos"),
    ("parmesan", "Parmesano", "Leche", "Ingrediente - Lácteos y huevos"),
    ("cheddar", "Cheddar", "Leche", "Ingrediente - Lácteos y huevos"),
    ("gouda", "Gouda", "Leche", "Ingrediente - Lácteos y huevos"),
    ("brie", "Brie", "Leche", "Ingrediente - Lácteos y huevos"),
    ("goat_cheese", "Queso de cabra", "Leche", "Ingrediente - Lácteos y huevos"),
    ("blue_cheese", "Queso azul", "Leche", "Ingrediente - Lácteos y huevos"),
    ("feta", "Feta", "Leche", "Ingrediente - Lácteos y huevos"),
    ("ricotta", "Ricotta", "Leche", "Ingrediente - Lácteos y huevos"),
    ("cream_cheese", "Queso crema", "Leche", "Ingrediente - Lácteos y huevos"),
    ("cream", "Nata", "Leche", "Ingrediente - Lácteos y huevos"),
    ("heavy_cream", "Nata para montar", "Leche", "Ingrediente - Lácteos y huevos"),
    ("sour_cream", "Crema agria", "Leche", "Ingrediente - Lácteos y huevos"),
    ("yogurt", "Yogur", "Leche", "Ingrediente - Lácteos y huevos"),
    ("whey", "Suero de leche", "Leche", "Ingrediente - Lácteos y huevos"),
    ("ghee", "Ghee", "Leche", "Ingrediente - Lácteos y huevos"),
    ("condensed_milk", "Leche condensada", "Leche", "Ingrediente - Lácteos y huevos"),
    ("ice_cream_scoop", "Bola de helado", "Huevos, Leche", "Ingrediente - Lácteos y huevos"),

    # === FISH & SEAFOOD ===
    ("shrimp", "Gamba", "Marisco", "Ingrediente - Pescado y marisco"),
    ("lobster", "Langosta", "Marisco", "Ingrediente - Pescado y marisco"),
    ("crab", "Cangrejo", "Marisco", "Ingrediente - Pescado y marisco"),
    ("clam", "Almeja", "Marisco, Moluscos", "Ingrediente - Pescado y marisco"),
    ("mussel", "Mejillón", "Marisco, Moluscos", "Ingrediente - Pescado y marisco"),
    ("scallop", "Vieira", "Marisco, Moluscos", "Ingrediente - Pescado y marisco"),
    ("squid", "Calamar", "Marisco, Moluscos", "Ingrediente - Pescado y marisco"),
    ("octopus", "Pulpo", "Marisco, Moluscos", "Ingrediente - Pescado y marisco"),
    ("prawn", "Langostino", "Marisco", "Ingrediente - Pescado y marisco"),
    ("langoustine", "Cigala", "Marisco", "Ingrediente - Pescado y marisco"),
    ("cockle", "Berberecho", "Marisco, Moluscos", "Ingrediente - Pescado y marisco"),
    ("crayfish", "Cangrejo de río", "Marisco", "Ingrediente - Pescado y marisco"),
    ("sea_urchin", "Erizo de mar", "Marisco", "Ingrediente - Pescado y marisco"),
    ("salmon", "Salmón", "Pescado", "Ingrediente - Pescado y marisco"),
    ("tuna", "Atún", "Pescado", "Ingrediente - Pescado y marisco"),
    ("sardine", "Sardina", "Pescado", "Ingrediente - Pescado y marisco"),
    ("anchovy", "Anchoa", "Pescado", "Ingrediente - Pescado y marisco"),
    ("cod", "Bacalao", "Pescado", "Ingrediente - Pescado y marisco"),
    ("mackerel", "Caballa", "Pescado", "Ingrediente - Pescado y marisco"),
    ("hake", "Merluza", "Pescado", "Ingrediente - Pescado y marisco"),
    ("trout", "Trucha", "Pescado", "Ingrediente - Pescado y marisco"),
    ("swordfish", "Pez espada", "Pescado", "Ingrediente - Pescado y marisco"),
    ("monkfish", "Rape", "Pescado", "Ingrediente - Pescado y marisco"),
    ("sea_bass", "Lubina", "Pescado", "Ingrediente - Pescado y marisco"),
    ("eel", "Anguila", "Pescado", "Ingrediente - Pescado y marisco"),
    ("herring", "Arenque", "Pescado", "Ingrediente - Pescado y marisco"),
    ("perch", "Perca", "Pescado", "Ingrediente - Pescado y marisco"),
    ("catfish", "Bagre", "Pescado", "Ingrediente - Pescado y marisco"),
    ("tilapia", "Tilapia", "Pescado", "Ingrediente - Pescado y marisco"),

    # === MEAT ===
    ("chicken_meat", "Pollo", "", "Ingrediente - Carne"),
    ("chicken_breast", "Pechuga de pollo", "", "Ingrediente - Carne"),
    ("chicken_thigh", "Muslo de pollo", "", "Ingrediente - Carne"),
    ("chicken_wing", "Ala de pollo", "", "Ingrediente - Carne"),
    ("turkey", "Pavo", "", "Ingrediente - Carne"),
    ("duck", "Pato", "", "Ingrediente - Carne"),
    ("beef", "Ternera", "", "Ingrediente - Carne"),
    ("beef_steak", "Filete de ternera", "", "Ingrediente - Carne"),
    ("ground_beef", "Carne picada", "", "Ingrediente - Carne"),
    ("veal", "Ternera lechal", "", "Ingrediente - Carne"),
    ("pork", "Cerdo", "", "Ingrediente - Carne"),
    ("pork_belly", "Panceta", "", "Ingrediente - Carne"),
    ("bacon", "Bacon", "", "Ingrediente - Carne"),
    ("ham", "Jamón cocido", "", "Ingrediente - Carne"),
    ("salami", "Salami", "", "Ingrediente - Carne"),
    ("sausage", "Salchicha", "", "Ingrediente - Carne"),
    ("lamb", "Cordero", "", "Ingrediente - Carne"),
    ("lamb_chop", "Chuleta de cordero", "", "Ingrediente - Carne"),
    ("lamb_leg", "Pierna de cordero", "", "Ingrediente - Carne"),
    ("rabbit", "Conejo", "", "Ingrediente - Carne"),
    ("venison", "Venado", "", "Ingrediente - Carne"),
    ("liver", "Hígado", "", "Ingrediente - Carne"),
    ("prosciutto", "Prosciutto", "", "Ingrediente - Carne"),
    ("pancetta", "Panceta italiana", "", "Ingrediente - Carne"),

    # === LEGUMES ===
    ("lentil", "Lenteja", "Lentejas, Legumbres", "Ingrediente - Legumbres"),
    ("chickpea", "Garbanzo", "Garbanzos, Legumbres", "Ingrediente - Legumbres"),
    ("lupin", "Altramuz", "Altramuz, Legumbres", "Ingrediente - Legumbres"),
    ("green_bean", "Judía verde", "Legumbres", "Ingrediente - Legumbres"),
    ("kidney_bean", "Alubia roja", "Legumbres", "Ingrediente - Legumbres"),
    ("green_pea", "Guisante", "Legumbres", "Ingrediente - Legumbres"),

    # === SEEDS ===
    ("sesame", "Sésamo", "Sésamo", "Ingrediente - Semillas"),
    ("mustard_seed", "Semilla de mostaza", "Mostaza", "Ingrediente - Semillas"),
    ("poppy_seed", "Semilla de amapola", "", "Ingrediente - Semillas"),
    ("sunflower_seed", "Pipa de girasol", "", "Ingrediente - Semillas"),
    ("flaxseed", "Linaza", "", "Ingrediente - Semillas"),
    ("chia_seed", "Chía", "", "Ingrediente - Semillas"),

    # === GRAINS & BREAD ===
    ("wheat", "Trigo", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("bread", "Pan", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("flour", "Harina", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("pasta", "Pasta", "Trigo, Gluten, Huevos", "Ingrediente - Cereales"),
    ("noodle", "Fideos", "Trigo, Gluten, Huevos", "Ingrediente - Cereales"),
    ("rice", "Arroz", "", "Ingrediente - Cereales"),
    ("corn", "Maíz", "Maíz", "Ingrediente - Cereales"),
    ("oat", "Avena", "Gluten", "Ingrediente - Cereales"),
    ("barley", "Cebada", "Gluten", "Ingrediente - Cereales"),
    ("buckwheat", "Trigo sarraceno", "Trigo sarraceno", "Ingrediente - Cereales"),
    ("quinoa", "Quinoa", "", "Ingrediente - Cereales"),
    ("couscous", "Cuscús", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("seitan", "Seitán", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("cornmeal", "Harina de maíz", "Maíz", "Ingrediente - Cereales"),
    ("tapioca", "Tapioca", "", "Ingrediente - Cereales"),
    ("baguette", "Baguette", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("sourdough", "Pan de masa madre", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("croissant", "Croissant", "Trigo, Gluten, Leche, Huevos", "Ingrediente - Cereales"),
    ("bagel", "Bagel", "Trigo, Gluten, Sésamo", "Ingrediente - Cereales"),
    ("tortilla", "Tortilla (wrap)", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("pita_bread", "Pan de pita", "Trigo, Gluten", "Ingrediente - Cereales"),
    ("naan", "Pan naan", "Trigo, Gluten, Leche", "Ingrediente - Cereales"),

    # === SOY ===
    ("soybean", "Soja", "Soja", "Ingrediente - Soja"),
    ("tofu", "Tofu", "Soja", "Ingrediente - Soja"),
    ("tempeh", "Tempeh", "Soja", "Ingrediente - Soja"),
    ("soy_sauce", "Salsa de soja", "Soja, Trigo, Gluten", "Ingrediente - Soja"),
    ("miso", "Miso", "Soja", "Ingrediente - Soja"),

    # === FRUITS ===
    ("apple", "Manzana", "", "Ingrediente - Fruta"),
    ("orange", "Naranja", "", "Ingrediente - Fruta"),
    ("banana", "Plátano", "Látex (reactividad cruzada)", "Ingrediente - Fruta"),
    ("strawberry", "Fresa", "", "Ingrediente - Fruta"),
    ("grape", "Uva", "Sulfitos", "Ingrediente - Fruta"),
    ("lemon", "Limón", "", "Ingrediente - Fruta"),
    ("lime", "Lima", "", "Ingrediente - Fruta"),
    ("mango", "Mango", "Látex (reactividad cruzada)", "Ingrediente - Fruta"),
    ("kiwi", "Kiwi", "Látex (reactividad cruzada)", "Ingrediente - Fruta"),
    ("avocado", "Aguacate", "Látex (reactividad cruzada)", "Ingrediente - Fruta"),
    ("papaya", "Papaya", "Látex (reactividad cruzada)", "Ingrediente - Fruta"),
    ("pineapple", "Piña", "", "Ingrediente - Fruta"),
    ("watermelon", "Sandía", "", "Ingrediente - Fruta"),
    ("melon", "Melón", "", "Ingrediente - Fruta"),
    ("peach", "Melocotón", "", "Ingrediente - Fruta"),
    ("cherry", "Cereza", "", "Ingrediente - Fruta"),
    ("pear", "Pera", "", "Ingrediente - Fruta"),
    ("plum", "Ciruela", "", "Ingrediente - Fruta"),
    ("apricot", "Albaricoque", "", "Ingrediente - Fruta"),
    ("fig", "Higo", "", "Ingrediente - Fruta"),
    ("date", "Dátil", "", "Ingrediente - Fruta"),
    ("pomegranate", "Granada", "", "Ingrediente - Fruta"),
    ("blueberry", "Arándano", "", "Ingrediente - Fruta"),
    ("raspberry", "Frambuesa", "", "Ingrediente - Fruta"),
    ("blackberry", "Mora", "", "Ingrediente - Fruta"),
    ("cranberry", "Arándano rojo", "", "Ingrediente - Fruta"),
    ("grapefruit", "Pomelo", "", "Ingrediente - Fruta"),
    ("tangerine", "Mandarina", "", "Ingrediente - Fruta"),
    ("raisin", "Pasa", "Sulfitos", "Ingrediente - Fruta"),
    ("prune", "Ciruela pasa", "Sulfitos", "Ingrediente - Fruta"),
    ("persimmon", "Caqui", "", "Ingrediente - Fruta"),
    ("guava", "Guayaba", "", "Ingrediente - Fruta"),
    ("lychee", "Lichi", "", "Ingrediente - Fruta"),
    ("dragonfruit", "Pitahaya", "", "Ingrediente - Fruta"),
    ("passion_fruit", "Maracuyá", "", "Ingrediente - Fruta"),

    # === VEGETABLES ===
    ("potato", "Patata", "", "Ingrediente - Verdura"),
    ("sweet_potato", "Boniato", "", "Ingrediente - Verdura"),
    ("carrot", "Zanahoria", "", "Ingrediente - Verdura"),
    ("broccoli", "Brócoli", "", "Ingrediente - Verdura"),
    ("cauliflower", "Coliflor", "", "Ingrediente - Verdura"),
    ("cabbage", "Col", "", "Ingrediente - Verdura"),
    ("spinach", "Espinacas", "", "Ingrediente - Verdura"),
    ("kale", "Kale", "", "Ingrediente - Verdura"),
    ("lettuce", "Lechuga", "", "Ingrediente - Verdura"),
    ("cucumber", "Pepino", "", "Ingrediente - Verdura"),
    ("zucchini", "Calabacín", "", "Ingrediente - Verdura"),
    ("eggplant", "Berenjena", "", "Ingrediente - Verdura"),
    ("bell_pepper", "Pimiento", "", "Ingrediente - Verdura"),
    ("chili_pepper", "Chile", "", "Ingrediente - Verdura"),
    ("jalapeno", "Jalapeño", "", "Ingrediente - Verdura"),
    ("pumpkin", "Calabaza", "", "Ingrediente - Verdura"),
    ("artichoke", "Alcachofa", "", "Ingrediente - Verdura"),
    ("asparagus", "Espárrago", "", "Ingrediente - Verdura"),
    ("tomato", "Tomate", "", "Ingrediente - Verdura"),
    ("onion", "Cebolla", "", "Ingrediente - Verdura"),
    ("garlic", "Ajo", "", "Ingrediente - Verdura"),
    ("mushroom", "Champiñón", "", "Ingrediente - Verdura"),
    ("celery", "Apio", "Apio", "Ingrediente - Verdura"),
    ("mustard_greens", "Hojas de mostaza", "Mostaza", "Ingrediente - Verdura"),
    ("leek", "Puerro", "", "Ingrediente - Verdura"),
    ("radish", "Rábano", "", "Ingrediente - Verdura"),
    ("turnip", "Nabo", "", "Ingrediente - Verdura"),
    ("beet", "Remolacha", "", "Ingrediente - Verdura"),
    ("ginger", "Jengibre", "", "Ingrediente - Verdura"),
    ("fennel", "Hinojo", "", "Ingrediente - Verdura"),
    ("okra", "Okra", "", "Ingrediente - Verdura"),

    # === CONDIMENTS ===
    ("mayonnaise", "Mayonesa", "Huevos", "Ingrediente - Condimento"),
    ("aioli", "Alioli", "Huevos", "Ingrediente - Condimento"),
    ("peanut_butter", "Mantequilla de cacahuete", "Cacahuetes", "Ingrediente - Condimento"),
    ("nutella", "Nutella", "Leche, Frutos secos", "Ingrediente - Condimento"),
    ("tahini", "Tahini", "Sésamo", "Ingrediente - Condimento"),
    ("hummus_dip", "Hummus", "Garbanzos, Sésamo", "Ingrediente - Condimento"),
    ("pesto", "Pesto", "Leche, Frutos secos", "Ingrediente - Condimento"),
    ("romesco", "Romesco", "Frutos secos", "Ingrediente - Condimento"),
    ("mustard_sauce", "Mostaza", "Mostaza", "Ingrediente - Condimento"),
    ("ketchup", "Ketchup", "", "Ingrediente - Condimento"),
    ("hot_sauce", "Salsa picante", "", "Ingrediente - Condimento"),
    ("fish_sauce", "Salsa de pescado", "Pescado", "Ingrediente - Condimento"),
    ("oyster_sauce", "Salsa de ostras", "Marisco", "Ingrediente - Condimento"),
    ("worcestershire", "Salsa Worcestershire", "Pescado", "Ingrediente - Condimento"),
    ("vinegar", "Vinagre", "Sulfitos", "Ingrediente - Condimento"),
    ("balsamic_vinegar", "Vinagre balsámico", "Sulfitos", "Ingrediente - Condimento"),

    # === SPICES ===
    ("black_pepper", "Pimienta negra", "", "Ingrediente - Especia"),
    ("cumin", "Comino", "", "Ingrediente - Especia"),
    ("paprika", "Pimentón", "", "Ingrediente - Especia"),
    ("smoked_paprika", "Pimentón ahumado", "", "Ingrediente - Especia"),
    ("cinnamon", "Canela", "", "Ingrediente - Especia"),
    ("saffron", "Azafrán", "", "Ingrediente - Especia"),
    ("vanilla", "Vainilla", "", "Ingrediente - Especia"),
    ("curry_powder", "Curry en polvo", "Mostaza", "Ingrediente - Especia"),
    ("za_atar", "Za'atar", "Sésamo", "Ingrediente - Especia"),
    ("salt", "Sal", "", "Ingrediente - Especia"),
    ("sugar", "Azúcar", "", "Ingrediente - Especia"),
    ("honey", "Miel", "", "Ingrediente - Especia"),
    ("chocolate", "Chocolate", "Leche, Soja", "Ingrediente - Especia"),
    ("olive_oil", "Aceite de oliva", "", "Ingrediente - Especia"),
    ("gelatin", "Gelatina", "", "Ingrediente - Especia"),
]

# =====================================================
# Generate CSV
# =====================================================
# Deduplicate by Spanish name, keeping the entry with more allergens
seen = {}
deduped = []
for entry in CATALOG:
    key, name, allergens, cat = entry
    if name in seen:
        old_key, old_allergens, old_cat, old_idx = seen[name]
        if len(allergens) > len(old_allergens):
            deduped[old_idx] = entry
            seen[name] = (key, allergens, cat, old_idx)
    else:
        seen[name] = (key, allergens, cat, len(deduped))
        deduped.append(entry)
CATALOG = deduped

# Build English translations
CATEGORY_EN = {
    "Plato español": "Spanish dish",
    "Plato internacional": "International dish",
    "Ingrediente - Frutos secos": "Ingredient - Nuts",
    "Ingrediente - Lácteos y huevos": "Ingredient - Dairy & Eggs",
    "Ingrediente - Pescado y marisco": "Ingredient - Fish & Seafood",
    "Ingrediente - Carne": "Ingredient - Meat",
    "Ingrediente - Legumbres": "Ingredient - Legumes",
    "Ingrediente - Semillas": "Ingredient - Seeds",
    "Ingrediente - Cereales": "Ingredient - Grains & Bread",
    "Ingrediente - Soja": "Ingredient - Soy",
    "Ingrediente - Fruta": "Ingredient - Fruit",
    "Ingrediente - Verdura": "Ingredient - Vegetable",
    "Ingrediente - Condimento": "Ingredient - Condiment",
    "Ingrediente - Especia": "Ingredient - Spice",
}

ALLERGEN_ES_TO_EN = {
    "Trigo": "Wheat", "Gluten": "Gluten", "Huevos": "Eggs", "Leche": "Milk",
    "Soja": "Soy", "Frutos secos": "Tree Nuts", "Cacahuetes": "Peanuts",
    "Pescado": "Fish", "Marisco": "Shellfish", "Moluscos": "Mollusks",
    "Sésamo": "Sesame", "Mostaza": "Mustard", "Apio": "Celery",
    "Altramuz": "Lupin", "Legumbres": "Legumes", "Lentejas": "Lentils",
    "Garbanzos": "Chickpeas", "Sulfitos": "Sulfites", "Maíz": "Corn",
    "Trigo sarraceno": "Buckwheat",
    "Látex (reactividad cruzada)": "Latex (food cross-reactive)",
}

def english_name(key):
    return key.replace("_", " ").title()

def allergens_to_english(es_allergens):
    if not es_allergens or es_allergens == "-":
        return "-"
    parts = [a.strip() for a in es_allergens.split(",")]
    en_parts = [ALLERGEN_ES_TO_EN.get(p, p) for p in parts]
    return ", ".join(en_parts)

csv_path = "/Users/xai/alertgia/alertgia_catalogo.csv"
with open(csv_path, 'w', newline='', encoding='utf-8') as f:
    writer = csv.writer(f)
    writer.writerow(["Nombre (ES)", "Nombre (EN)", "Alérgenos (ES)", "Allergens (EN)", "Categoría (ES)", "Category (EN)"])
    for key, spanish_name, allergens, category in sorted(CATALOG, key=lambda x: (x[3], x[1])):
        en_name = english_name(key)
        en_allergens = allergens_to_english(allergens)
        en_category = CATEGORY_EN.get(category, category)
        writer.writerow([
            spanish_name, en_name,
            allergens if allergens else "-", en_allergens,
            category, en_category
        ])

print(f"CSV saved: {csv_path}")

# =====================================================
# Generate PDF (landscape)
# =====================================================
pdf_path = "/Users/xai/alertgia/alertgia_catalogo.pdf"

class PDF(FPDF):
    def header(self):
        self.set_font('ArialB', '', 14)
        self.cell(0, 10, 'Alertgia - Catálogo de Alimentos y Alérgenos', new_x="LMARGIN", new_y="NEXT", align='C')
        self.ln(2)

    def footer(self):
        self.set_y(-15)
        self.set_font('ArialUni', '', 8)
        self.cell(0, 10, f'Página {self.page_no()}/{{nb}}', align='C')

pdf = PDF(orientation='L', unit='mm', format='A4')
pdf.add_font('ArialUni', '', '/System/Library/Fonts/Supplemental/Arial Unicode.ttf')
pdf.add_font('ArialB', '', '/System/Library/Fonts/Supplemental/Arial Bold.ttf')
pdf.alias_nb_pages()
pdf.set_auto_page_break(auto=True, margin=20)
pdf.add_page()

# Column widths for landscape A4 (297mm - 20mm margins = 277mm), 6 columns
col_w = [48, 48, 48, 48, 42, 43]
headers = ["Nombre (ES)", "Name (EN)", "Alérgenos (ES)", "Allergens (EN)", "Categoría (ES)", "Category (EN)"]

# Table header
pdf.set_font('ArialB', '', 6)
pdf.set_fill_color(0, 150, 136)  # Teal
pdf.set_text_color(255, 255, 255)
for i, h in enumerate(headers):
    pdf.cell(col_w[i], 8, h, border=1, fill=True, align='C')
pdf.ln()

# Table rows
pdf.set_text_color(0, 0, 0)
current_category = ""
row_count = 0

for key, spanish_name, allergens, category in sorted(CATALOG, key=lambda x: (x[3], x[1])):
    en_name = english_name(key)
    en_allergens = allergens_to_english(allergens)
    en_category = CATEGORY_EN.get(category, category)

    # Category separator
    if category != current_category:
        current_category = category
        pdf.set_font('ArialB', '', 6)
        pdf.set_fill_color(230, 230, 230)
        pdf.cell(sum(col_w), 6, f'  {category} / {en_category}', border=1, fill=True, new_x="LMARGIN", new_y="NEXT")

    pdf.set_font('ArialUni', '', 6)
    pdf.set_fill_color(255, 255, 255) if row_count % 2 == 0 else pdf.set_fill_color(245, 245, 245)

    pdf.cell(col_w[0], 5, spanish_name, border=1, fill=True)
    pdf.cell(col_w[1], 5, en_name, border=1, fill=True)

    # Allergens in red
    if allergens:
        pdf.set_text_color(200, 0, 0)
        pdf.cell(col_w[2], 5, allergens, border=1, fill=True)
        pdf.cell(col_w[3], 5, en_allergens, border=1, fill=True)
        pdf.set_text_color(0, 0, 0)
    else:
        pdf.cell(col_w[2], 5, "-", border=1, fill=True)
        pdf.cell(col_w[3], 5, "-", border=1, fill=True)

    pdf.cell(col_w[4], 5, category, border=1, fill=True)
    pdf.cell(col_w[5], 5, en_category, border=1, fill=True, new_x="LMARGIN", new_y="NEXT")
    row_count += 1

pdf.output(pdf_path)
print(f"PDF saved: {pdf_path}")
print(f"Total entries: {len(CATALOG)}")
