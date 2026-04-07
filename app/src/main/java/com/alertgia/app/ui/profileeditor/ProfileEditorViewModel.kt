package com.alertgia.app.ui.profileeditor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alertgia.app.data.repository.UserProfileRepository
import com.alertgia.app.domain.model.Allergy
import com.alertgia.app.domain.model.DietaryRestriction
import com.alertgia.app.domain.model.Severity
import com.alertgia.app.domain.model.UserProfile
import com.alertgia.app.ui.theme.AvatarColors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileEditorUiState(
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val name: String = "",
    val avatarColor: Long = AvatarColors.first(),
    val allergies: List<Allergy> = emptyList(),
    val dietaryRestrictions: Set<DietaryRestriction> = emptySet(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,
    val showDeleteDialog: Boolean = false
)

@HiltViewModel
class ProfileEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UserProfileRepository
) : ViewModel() {

    private val profileId: Long = savedStateHandle["profileId"] ?: -1L

    private val _uiState = MutableStateFlow(ProfileEditorUiState())
    val uiState: StateFlow<ProfileEditorUiState> = _uiState.asStateFlow()

    init {
        if (profileId > 0) {
            loadProfile()
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val profile = repository.getProfile(profileId)
            if (profile != null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isEditing = true,
                        name = profile.name,
                        avatarColor = profile.avatarColor,
                        allergies = profile.allergies,
                        dietaryRestrictions = profile.dietaryRestrictions
                    )
                }
            } else {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateAvatarColor(color: Long) {
        _uiState.update { it.copy(avatarColor = color) }
    }

    fun toggleAllergy(allergyName: String) {
        _uiState.update { state ->
            val existing = state.allergies.find { it.name == allergyName }
            if (existing != null) {
                state.copy(allergies = state.allergies.filter { it.name != allergyName })
            } else {
                state.copy(allergies = state.allergies + Allergy(name = allergyName))
            }
        }
    }

    fun updateAllergySeverity(allergyName: String, severity: Severity) {
        _uiState.update { state ->
            state.copy(
                allergies = state.allergies.map {
                    if (it.name == allergyName) it.copy(severity = severity) else it
                }
            )
        }
    }

    fun addCustomAllergy(name: String) {
        if (name.isBlank()) return
        val trimmed = name.trim()
        _uiState.update { state ->
            if (state.allergies.any { it.name.equals(trimmed, ignoreCase = true) }) {
                state
            } else {
                state.copy(allergies = state.allergies + Allergy(name = trimmed))
            }
        }
    }

    fun toggleDietaryRestriction(restriction: DietaryRestriction) {
        _uiState.update { state ->
            val current = state.dietaryRestrictions
            if (restriction in current) {
                state.copy(dietaryRestrictions = current - restriction)
            } else {
                state.copy(dietaryRestrictions = current + restriction)
            }
        }
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) return

        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            repository.saveProfile(
                UserProfile(
                    id = if (profileId > 0) profileId else 0,
                    name = state.name.trim(),
                    avatarColor = state.avatarColor,
                    allergies = state.allergies,
                    dietaryRestrictions = state.dietaryRestrictions
                )
            )
            _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteProfile(onDeleted: () -> Unit) {
        if (profileId <= 0) return
        viewModelScope.launch {
            repository.deleteProfile(profileId)
            onDeleted()
        }
    }
}
