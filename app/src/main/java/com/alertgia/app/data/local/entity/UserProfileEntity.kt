package com.alertgia.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val avatarColor: Long = 0xFF009688,
    val dietaryRestrictions: String = "" // Comma-separated DietaryRestriction names
)
