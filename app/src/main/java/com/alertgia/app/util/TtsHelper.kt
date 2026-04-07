package com.alertgia.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TtsHelper @Inject constructor(
    private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    fun initialize() {
        tts = TextToSpeech(context) { status ->
            isReady = status == TextToSpeech.SUCCESS
        }
    }

    fun setLanguage(lang: String) {
        val locale = when (lang) {
            "es" -> Locale("es", "ES")
            else -> Locale.ENGLISH
        }
        tts?.language = locale
    }

    fun speak(text: String) {
        if (!isReady) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "alertgia_tts")
    }

    fun speakAllergenAlert(allergens: List<String>, lang: String) {
        if (!isReady || allergens.isEmpty()) return
        val message = when (lang) {
            "es" -> "Alerta. Se detectaron los siguientes alérgenos: ${allergens.joinToString(", ")}"
            else -> "Alert. The following allergens were detected: ${allergens.joinToString(", ")}"
        }
        speak(message)
    }

    fun speakSafe(lang: String) {
        if (!isReady) return
        val message = when (lang) {
            "es" -> "Seguro. No se detectaron alérgenos."
            else -> "Safe. No allergens detected."
        }
        speak(message)
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}
