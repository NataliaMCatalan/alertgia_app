package com.alertgia.app.domain.model

data class UserProfile(
    val id: Long = 0,
    val name: String,
    val avatarColor: Long = 0xFF009688,
    val allergies: List<Allergy> = emptyList(),
    val dietaryRestrictions: Set<DietaryRestriction> = emptySet()
)
