package com.example.tictaclearn.data.datastore

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.tictaclearn.domain.model.Mood
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

// Claves para persistencia
val MOOD_ID_KEY = stringPreferencesKey("current_daily_mood_id")
val LAST_OPENED_EPOCH_DAY_KEY = longPreferencesKey("last_opened_epoch_day") // Para guardar la fecha
val PROGRESSION_INDEX_KEY = intPreferencesKey("daily_progression_index") // Para guardar el paso del ciclo (0-4)

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

    // --- Escritura ---

    suspend fun saveMoodId(moodId: String) {
        settingsDataStore.edit { preferences ->
            preferences[MOOD_ID_KEY] = moodId
        }
    }

    /**
     * 🔄 LÓGICA DE PROGRESIÓN DIARIA
     * Verifica si es un nuevo día. Si lo es, avanza el índice de la secuencia
     * y actualiza el Mood actual automáticamente.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateDailyMoodSequenceIfNeeded() {
        val currentEpochDay = LocalDate.now().toEpochDay() // Días desde 1970 (independiente de hora)

        settingsDataStore.edit { prefs ->
            val lastEpochDay = prefs[LAST_OPENED_EPOCH_DAY_KEY] ?: 0L

            // Si la fecha actual es posterior a la última guardada, es un nuevo día.
            if (currentEpochDay > lastEpochDay) {

                // Obtenemos el índice anterior de la secuencia (NO del mood actual, sino del ciclo oculto)
                // Si no existe (primera vez), usamos -1 para que el cálculo inicial dé el índice correcto.
                // Queremos empezar en NORMAL (índice 2 en la lista ALL_MOODS).
                val storedIndex = prefs[PROGRESSION_INDEX_KEY]

                val newIndex = if (storedIndex == null) {
                    // DÍA 1: Primer inicio de la app -> Empezamos en NORMAL (índice 2)
                    2
                } else {
                    // DÍAS SIGUIENTES: Sumar 1 y rotar (Ciclo: 2->3->4->0->1->2...)
                    // La lista es: [SOMNOLIENTO(0), RELAJADO(1), NORMAL(2), ATENTO(3), CONCENTRADO(4)]
                    (storedIndex + 1) % Mood.ALL_MOODS.size
                }

                // Obtenemos el Mood correspondiente a este nuevo índice
                val newMood = Mood.ALL_MOODS[newIndex]

                // Guardamos todo:
                // 1. La fecha de hoy para no repetir esto hasta mañana
                prefs[LAST_OPENED_EPOCH_DAY_KEY] = currentEpochDay
                // 2. El nuevo índice del ciclo (para calcular mañana)
                prefs[PROGRESSION_INDEX_KEY] = newIndex
                // 3. Actualizamos el Mood visible (esto resetea cualquier cambio manual de ayer)
                prefs[MOOD_ID_KEY] = newMood.id
            }
        }
    }
}