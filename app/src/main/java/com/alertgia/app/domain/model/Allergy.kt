package com.alertgia.app.domain.model

data class Allergy(
    val id: Long = 0,
    val name: String,
    val severity: Severity = Severity.MODERATE
)

enum class Severity(val displayName: String) {
    MILD("Mild"),
    MODERATE("Moderate"),
    SEVERE("Severe")
}

val COMMON_ALLERGENS = listOf(
    "Peanuts",
    "Tree Nuts",
    "Milk",
    "Eggs",
    "Wheat",
    "Soy",
    "Fish",
    "Shellfish",
    "Sesame",
    "Gluten",
    "Mustard",
    "Celery",
    "Lupin",
    "Legumes",
    "Lentils",
    "Chickpeas",
    "Mollusks",
    "Sulfites",
    "Corn",
    "Buckwheat",
    "Latex (food cross-reactive)"
)
