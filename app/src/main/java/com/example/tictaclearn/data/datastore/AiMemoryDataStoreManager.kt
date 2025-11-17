package com.example.tictaclearn.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// IMPORTACIONES CRÍTICAS: Si estas faltan, el proyecto no compila.
import com.example.tictaclearn.data.datastore.AiMemory
import com.example.tictaclearn.data.datastore.QTable


private val Context.aiMemoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "ai_memory")

private val Q_TABLE_KEY = stringPreferencesKey("ai_q_table")
private const val EMPTY_Q_TABLE_JSON = "{\"qTable\":{}}"

@Singleton
class AiMemoryDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val memoryDataStore = context.aiMemoryDataStore

    /**
     * Carga la Q-Table deserializando el objeto AiMemory.
     * @return La QTable deserializada (Map<String, List<Double>>).
     */
    suspend fun getQTable(): QTable {
        val jsonString = memoryDataStore.data
            .map { preferences ->
                preferences[Q_TABLE_KEY] ?: EMPTY_Q_TABLE_JSON
            }.first()

        // Si el JSON es EMPTY_Q_TABLE_JSON, devolvemos un mapa vacío
        return if (jsonString == EMPTY_Q_TABLE_JSON) {
            emptyMap()
        } else {
            // Deserializamos el objeto AiMemory (wrapper) y extraemos la QTable
            Json.decodeFromString<AiMemory>(jsonString).qTable
        }
    }

    /**
     * Guarda la Q-Table serializando el objeto AiMemory.
     */
    suspend fun saveQTable(qTable: QTable) {
        // Creamos el wrapper AiMemory para serializar el mapa correctamente
        val aiMemory = AiMemory(qTable = qTable)
        memoryDataStore.edit { preferences ->
            // Serializamos el objeto AiMemory completo
            preferences[Q_TABLE_KEY] = Json.encodeToString(aiMemory)
        }
    }

    /**
     * Borra la memoria.
     */
    suspend fun clearQTable() {
        memoryDataStore.edit { preferences ->
            preferences.remove(Q_TABLE_KEY)
        }
    }
}