package com.alertgia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.alertgia.app.data.local.entity.ProfileWithAllergies
import com.alertgia.app.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Transaction
    @Query("SELECT * FROM user_profiles ORDER BY name ASC")
    fun getAllProfilesWithAllergies(): Flow<List<ProfileWithAllergies>>

    @Transaction
    @Query("SELECT * FROM user_profiles WHERE id = :profileId")
    suspend fun getProfileWithAllergies(profileId: Long): ProfileWithAllergies?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: UserProfileEntity)

    @Delete
    suspend fun deleteProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :profileId")
    suspend fun deleteProfileById(profileId: Long)
}
