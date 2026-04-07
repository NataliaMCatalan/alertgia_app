package com.alertgia.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ProfileWithAllergies(
    @Embedded val profile: UserProfileEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "profileId"
    )
    val allergies: List<AllergyEntity>
)
