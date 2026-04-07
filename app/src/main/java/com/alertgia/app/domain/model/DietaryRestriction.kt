package com.alertgia.app.domain.model

enum class DietaryRestriction(
    val displayNameEn: String,
    val displayNameEs: String,
    val restrictedIngredients: Set<String>
) {
    KOSHER(
        "Kosher",
        "Kosher",
        setOf("Pork", "Shellfish", "Gelatin (pork)")
    ),
    HALAL(
        "Halal",
        "Halal",
        setOf("Pork", "Alcohol", "Gelatin (pork)", "Lard")
    ),
    VEGAN(
        "Vegan",
        "Vegano",
        setOf("Meat", "Fish", "Shellfish", "Milk", "Eggs", "Honey", "Gelatin", "Butter", "Cheese", "Cream")
    ),
    VEGETARIAN(
        "Vegetarian",
        "Vegetariano",
        setOf("Meat", "Fish", "Shellfish", "Gelatin")
    ),
    PESCATARIAN(
        "Pescatarian",
        "Pescetariano",
        setOf("Meat", "Pork", "Poultry")
    ),
    HINDU(
        "Hindu",
        "Hindú",
        setOf("Beef", "Pork")
    ),
    BUDDHIST(
        "Buddhist",
        "Budista",
        setOf("Meat", "Fish", "Garlic", "Onion", "Leek")
    ),
    LACTOSE_FREE(
        "Lactose-Free",
        "Sin Lactosa",
        setOf("Milk", "Cheese", "Cream", "Butter", "Yogurt", "Ice cream")
    ),
    LOW_FODMAP(
        "Low FODMAP",
        "Bajo en FODMAP",
        setOf("Garlic", "Onion", "Wheat", "Milk", "Honey", "Apple", "Pear", "Watermelon")
    ),
    KETO(
        "Keto",
        "Keto",
        setOf("Bread", "Rice", "Pasta", "Sugar", "Potato", "Corn")
    ),
    PALEO(
        "Paleo",
        "Paleo",
        setOf("Grains", "Wheat", "Rice", "Corn", "Dairy", "Sugar", "Legumes", "Soy")
    );

    companion object {
        fun getAllRestricted(restrictions: Set<DietaryRestriction>): Set<String> {
            return restrictions.flatMap { it.restrictedIngredients }.toSet()
        }
    }
}
