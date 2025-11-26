package com.chifuz.tictaclearn.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.aiMemoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_memory")

private val AI_MEMORY_KEY = stringPreferencesKey("ai_memory_data")
// Usamos el JSON de un objeto AiMemory por defecto para el "empty state"
private const val EMPTY_AI_MEMORY_JSON = "{\"qTable\":{},\"gamesPlayedCount\":0}"

@Singleton
class AiMemoryDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val memoryDataStore = context.aiMemoryDataStore

    /**
     * 🚀 NUEVO: Carga el objeto AiMemory completo (QTable + Contador).
     */
    suspend fun getAiMemory(): AiMemory {
        val jsonString = memoryDataStore.data
            .map { preferences ->
                preferences[AI_MEMORY_KEY] ?: EMPTY_AI_MEMORY_JSON
            }.first()

        // Deserializamos el objeto AiMemory completo
        return try {
            Json.decodeFromString<AiMemory>(jsonString)
        } catch (e: Exception) {
            // Manejar un error de deserialización o formato viejo, devolviendo el valor por defecto
            Json.decodeFromString(EMPTY_AI_MEMORY_JSON)
        }
    }

    /**
     * 🚀 NUEVO: Guarda el objeto AiMemory completo (QTable + Contador).
     */
    suspend fun saveAiMemory(aiMemory: AiMemory) {
        memoryDataStore.edit { preferences ->
            preferences[AI_MEMORY_KEY] = Json.encodeToString(aiMemory)
        }
    }

    /**
     * Borra la memoria, reseteando la QTable y el contador.
     */
    suspend fun clearAiMemory() {
        memoryDataStore.edit { preferences ->
            // Guardamos el JSON de un AiMemory vacío (qTable vacía, contador a 0).
            preferences[AI_MEMORY_KEY] = EMPTY_AI_MEMORY_JSON
        }
    }
}