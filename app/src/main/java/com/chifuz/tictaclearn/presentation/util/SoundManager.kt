package com.chifuz.tictaclearn.presentation.util

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import com.chifuz.tictaclearn.R
import com.chifuz.tictaclearn.data.datastore.MoodDataStoreManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    @ApplicationContext context: Context,
    private val preferences: MoodDataStoreManager
) {
    private val soundPool: SoundPool
    private val soundMap = mutableMapOf<SoundType, Int>()
    private val loadedSounds = mutableSetOf<Int>() // Para saber si ya cargó
    private var isSoundEnabled = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        // Escuchar cuando un sonido termina de cargar
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
                Log.d("SoundManager", "Sonido cargado ID: $sampleId")
            } else {
                Log.e("SoundManager", "Error cargando sonido ID: $sampleId")
            }
        }

        // Cargar sonidos
        soundMap[SoundType.CLICK] = soundPool.load(context, R.raw.sfx_ui_click, 1)
        soundMap[SoundType.PLACE] = soundPool.load(context, R.raw.sfx_place_pop, 1)
        soundMap[SoundType.WIN] = soundPool.load(context, R.raw.sfx_win_fanfare, 1)
        soundMap[SoundType.LOSE] = soundPool.load(context, R.raw.sfx_lose_retro, 1)

        CoroutineScope(Dispatchers.IO).launch {
            preferences.isSoundEnabled.collect { isEnabled ->
                isSoundEnabled = isEnabled
                Log.d("SoundManager", "Sonido habilitado: $isEnabled")
            }
        }
    }

    fun play(type: SoundType) {
        if (!isSoundEnabled) {
            Log.d("SoundManager", "Intento de reproducir $type pero el sonido está desactivado.")
            return
        }

        val soundId = soundMap[type] ?: return

        if (loadedSounds.contains(soundId)) {
            // priority 1, loop 0, rate 1f
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            Log.d("SoundManager", "Reproduciendo: $type")
        } else {
            Log.w("SoundManager", "El sonido $type (ID $soundId) aún no está cargado.")
        }
    }

    fun release() {
        soundPool.release()
    }
}

enum class SoundType {
    CLICK, PLACE, WIN, LOSE
}