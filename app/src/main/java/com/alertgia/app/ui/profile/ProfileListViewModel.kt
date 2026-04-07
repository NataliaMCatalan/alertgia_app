package com.alertgia.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alertgia.app.data.repository.UserProfileRepository
import com.alertgia.app.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileListUiState {
    data object Loading : ProfileListUiState
    data object Empty : ProfileListUiState
    data class Success(val profiles: List<UserProfile>) : ProfileListUiState
}

@HiltViewModel
class ProfileListViewModel @Inject constructor(
    private val repository: UserProfileRepository
) : ViewModel() {

    val uiState: StateFlow<ProfileListUiState> = repository.getAllProfiles()
        .map { profiles ->
            if (profiles.isEmpty()) {
                ProfileListUiState.Empty
            } else {
                ProfileListUiState.Success(profiles)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileListUiState.Loading
        )

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch {
            repository.deleteProfile(profileId)
        }
    }
}
