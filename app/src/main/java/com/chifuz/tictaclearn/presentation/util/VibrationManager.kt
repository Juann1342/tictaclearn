package com.chifuz.tictaclearn.presentation.util


import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.chifuz.tictaclearn.data.datastore.MoodDataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationManager @Inject constructor(
    @ApplicationContext context: Context,
    private val preferences: MoodDataStoreManager
) {
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private var isVibrationEnabled = true

    init {
        CoroutineScope(Dispatchers.IO).launch {
            preferences.isVibrationEnabled.collect { isEnabled ->
                isVibrationEnabled = isEnabled
            }
        }
    }

    fun vibrateClick() {
        if (!isVibrationEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            vibrator.vibrate(20) // Fallback old android
        }
    }

    fun vibrateMove() {
        if (!isVibrationEnabled) return
        // Un golpe seco un poco más fuerte que el click
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(40)
        }
    }

    fun vibrateWin() {
        if (!isVibrationEnabled) return
        // Patrón de victoria: ta-ta-taaa
        val timings = longArrayOf(0, 100, 50, 100, 50, 300)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255) // Solo funciona en API 26+

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            vibrator.vibrate(timings, -1)
        }
    }

    fun vibrateLose() {
        if (!isVibrationEnabled) return
        // Patrón de derrota: uno largo y pesado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, 100))
        } else {
            vibrator.vibrate(500)
        }
    }
}