package com.example.tictaclearn.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tictaclearn.domain.model.Mood
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Definimos la clave para guardar el Mood ID
val MOOD_ID_KEY = stringPreferencesKey("current_daily_mood_id")

@Singleton
class MoodDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Inicializamos el DataStore usando el delegado de propiedad
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val settingsDataStore = context.dataStore

    // --- Métodos de Lectura ---

    suspend fun getMoodId(): String {
        return settingsDataStore.data
            .map { preferences ->
                // Accedemos a la clave con el operador []
                preferences[MOOD_ID_KEY] ?: Mood.getDefaultDailyMood().id
            }
            .first()
    }

    // --- Métodos de Escritura ---

    suspend fun saveMoodId(moodId: String) {
        settingsDataStore.edit { preferences ->
            preferences[MOOD_ID_KEY] = moodId
        }
    }
}