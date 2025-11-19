package com.example.tictaclearn.domain.usecase

import com.example.tictaclearn.domain.model.Mood
import com.example.tictaclearn.domain.repository.AIEngineRepository
import android.util.Log // Importamos Log para debug

// domain/usecase/ConfigurationUseCase.kt

private const val TAG = "ConfigUseCase"

class ConfigurationUseCase(
    // Dependemos del repositorio para acceder a los datos de la IA
    private val aiEngineRepository: AIEngineRepository
) {

    // 1. Obtener el Estado de Ánimo del Día
    // La UI llamará a esto para saber qué Mood mostrar.
    suspend fun getCurrentMood(): Mood? {
        return try {
            // Asumimos que el Repositorio devuelve Mood (no nullable) según su contrato.
            aiEngineRepository.getDailyMood()
        } catch (e: Exception) {
            // Si la llamada al repositorio falla (ej. error de DataStore),
            // devolvemos un valor seguro (NORMAL) en el dominio.
            Log.e(TAG, "Error al obtener el Mood diario, usando valor por defecto.", e)
            Mood.NORMAL
        }
    }

    // 2. Guardar el Estado de Ánimo Seleccionado
    // La UI llamará a esto cuando el usuario elija un Mood diferente.
    suspend fun selectNewMood(mood: Mood) {
        // Le pedimos al repositorio que guarde esta selección
        aiEngineRepository.saveDailyMood(mood)
    }

    // 3. Borrar la Memoria de la IA
    // La UI llama a esto cuando se presiona el botón "Borrar Memoria".
    suspend fun resetAILearning() {
        // Le pedimos al repositorio que ejecute la lógica de borrado
        aiEngineRepository.clearMemory()

        // **IMPORTANTE**: La memoria borrada no cambia el estado de ánimo (mood),
        // solo el conocimiento que tenía.
    }
}