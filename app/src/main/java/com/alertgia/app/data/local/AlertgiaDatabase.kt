package com.alertgia.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.alertgia.app.data.local.dao.AllergyDao
import com.alertgia.app.data.local.dao.UserProfileDao
import com.alertgia.app.data.local.entity.AllergyEntity
import com.alertgia.app.data.local.entity.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class, AllergyEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AlertgiaDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun allergyDao(): AllergyDao
}
