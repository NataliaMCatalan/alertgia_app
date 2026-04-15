package com.alertgia.app.ui.smartmenu

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alertgia.app.data.repository.UserProfileRepository
import com.alertgia.app.domain.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartMenuViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val profileId: Long = savedStateHandle["profileId"] ?: -1L

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            _profile.value = if (profileId == -1L) {
                profileRepository.getAllProfiles().first().firstOrNull()
            } else {
                profileRepository.getProfile(profileId)
            }
        }
    }
}
