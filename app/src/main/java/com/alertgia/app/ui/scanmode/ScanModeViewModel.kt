package com.alertgia.app.ui.scanmode

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
class ScanModeViewModel @Inject constructor(
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            _profile.value = profileRepository.getAllProfiles().first().firstOrNull()
        }
    }
}
