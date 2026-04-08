package com.alertgia.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "alertgia_prefs")

@Singleton
class AppPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        val LANGUAGE = stringPreferencesKey("language")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val RGPD_ACCEPTED = booleanPreferencesKey("rgpd_accepted")
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[LANGUAGE] ?: "en"
    }

    val ttsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[TTS_ENABLED] ?: false
    }

    val onboardingComplete: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE] ?: false
    }

    val rgpdAccepted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[RGPD_ACCEPTED] ?: false
    }

    suspend fun setLanguage(lang: String) {
        context.dataStore.edit { prefs -> prefs[LANGUAGE] = lang }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[TTS_ENABLED] = enabled }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setRgpdAccepted(accepted: Boolean) {
        context.dataStore.edit { prefs -> prefs[RGPD_ACCEPTED] = accepted }
    }
}
