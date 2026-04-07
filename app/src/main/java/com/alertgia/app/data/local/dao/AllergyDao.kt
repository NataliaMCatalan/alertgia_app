package com.alertgia.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alertgia.app.data.local.entity.AllergyEntity

@Dao
interface AllergyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergies(allergies: List<AllergyEntity>)

    @Query("DELETE FROM allergies WHERE profileId = :profileId")
    suspend fun deleteAllergiesForProfile(profileId: Long)

    @Query("SELECT * FROM allergies WHERE profileId = :profileId")
    suspend fun getAllergiesForProfile(profileId: Long): List<AllergyEntity>
}
