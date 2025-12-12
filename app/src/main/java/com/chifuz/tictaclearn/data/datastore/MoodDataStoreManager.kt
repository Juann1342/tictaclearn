package com.chifuz.tictaclearn.data.datastore

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.chifuz.tictaclearn.domain.model.Mood
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// Claves para persistencia
val MOOD_ID_KEY = stringPreferencesKey("current_daily_mood_id")
val LAST_OPENED_EPOCH_DAY_KEY = longPreferencesKey("last_opened_epoch_day")
val PROGRESSION_INDEX_KEY = intPreferencesKey("daily_progression_index")

// 🚀 NUEVAS CLAVES PARA AUDIO/HAPTICS
val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")

@Singleton
class MoodDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val settingsDataStore = context.dataStore

    // --- Lectura ---

    suspend fun getMoodId(): String {
        return settingsDataStore.data
            .map { preferences ->
                preferences[MOOD_ID_KEY] ?: Mood.getDefaultDailyMood().id
            }
            .first()
    }

    // 🚀 LECTURA DE SETTINGS (Flow para reactividad)
    val isSoundEnabled: Flow<Boolean> = settingsDataStore.data.map { prefs ->
        prefs[SOUND_ENABLED_KEY] ?: true // Por defecto activado
    }

    val isVibrationEnabled: Flow<Boolean> = settingsDataStore.data.map { prefs ->
        prefs[VIBRATION_ENABLED_KEY] ?: true // Por defecto activado
    }

    // --- Escritura ---

    suspend fun saveMoodId(moodId: String) {
        settingsDataStore.edit { preferences ->
            preferences[MOOD_ID_KEY] = moodId
        }
    }

    // 🚀 NUEVAS FUNCIONES DE ESCRITURA
    suspend fun setSoundEnabled(enabled: Boolean) {
        settingsDataStore.edit { prefs -> prefs[SOUND_ENABLED_KEY] = enabled }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        settingsDataStore.edit { prefs -> prefs[VIBRATION_ENABLED_KEY] = enabled }
    }

    /**
     * 🔄 LÓGICA DE PROGRESIÓN DIARIA
     * (MANTENIDA EXACTAMENTE IGUAL)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateDailyMoodSequenceIfNeeded() {
        val currentEpochDay = LocalDate.now().toEpochDay()

        settingsDataStore.edit { prefs ->
            val lastEpochDay = prefs[LAST_OPENED_EPOCH_DAY_KEY] ?: 0L

            if (currentEpochDay > lastEpochDay) {
                val storedIndex = prefs[PROGRESSION_INDEX_KEY]

                val newIndex = if (storedIndex == null) {
                    2
                } else {
                    (storedIndex + 1) % Mood.ALL_MOODS.size
                }
                val newMood = Mood.ALL_MOODS[newIndex]

                prefs[LAST_OPENED_EPOCH_DAY_KEY] = currentEpochDay
                prefs[PROGRESSION_INDEX_KEY] = newIndex
                prefs[MOOD_ID_KEY] = newMood.id
            }
        }
    }
}