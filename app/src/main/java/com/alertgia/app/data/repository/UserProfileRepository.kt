package com.alertgia.app.data.repository

import com.alertgia.app.data.local.dao.AllergyDao
import com.alertgia.app.data.local.dao.UserProfileDao
import com.alertgia.app.data.local.entity.AllergyEntity
import com.alertgia.app.data.local.entity.UserProfileEntity
import com.alertgia.app.domain.model.Allergy
import com.alertgia.app.domain.model.DietaryRestriction
import com.alertgia.app.domain.model.Severity
import com.alertgia.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val profileDao: UserProfileDao,
    private val allergyDao: AllergyDao
) {

    fun getAllProfiles(): Flow<List<UserProfile>> {
        return profileDao.getAllProfilesWithAllergies().map { list ->
            list.map { it.toDomain() }
        }
    }

    suspend fun getProfile(profileId: Long): UserProfile? {
        return profileDao.getProfileWithAllergies(profileId)?.toDomain()
    }

    suspend fun saveProfile(profile: UserProfile): Long {
        val restrictionsStr = profile.dietaryRestrictions.joinToString(",") { it.name }

        val profileId = if (profile.id == 0L) {
            profileDao.insertProfile(
                UserProfileEntity(
                    name = profile.name,
                    avatarColor = profile.avatarColor,
                    dietaryRestrictions = restrictionsStr
                )
            )
        } else {
            profileDao.updateProfile(
                UserProfileEntity(
                    id = profile.id,
                    name = profile.name,
                    avatarColor = profile.avatarColor,
                    dietaryRestrictions = restrictionsStr
                )
            )
            profile.id
        }

        allergyDao.deleteAllergiesForProfile(profileId)
        allergyDao.insertAllergies(
            profile.allergies.map { allergy ->
                AllergyEntity(
                    profileId = profileId,
                    allergyName = allergy.name,
                    severity = allergy.severity.name
                )
            }
        )

        return profileId
    }

    suspend fun deleteProfile(profileId: Long) {
        profileDao.deleteProfileById(profileId)
    }

    private fun com.alertgia.app.data.local.entity.ProfileWithAllergies.toDomain(): UserProfile {
        val restrictions = profile.dietaryRestrictions
            .split(",")
            .filter { it.isNotBlank() }
            .mapNotNull { name ->
                try { DietaryRestriction.valueOf(name) } catch (_: Exception) { null }
            }
            .toSet()

        return UserProfile(
            id = profile.id,
            name = profile.name,
            avatarColor = profile.avatarColor,
            allergies = allergies.map { entity ->
                Allergy(
                    id = entity.id,
                    name = entity.allergyName,
                    severity = Severity.valueOf(entity.severity)
                )
            },
            dietaryRestrictions = restrictions
        )
    }
}
