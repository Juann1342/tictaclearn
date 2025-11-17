package com.example.tictaclearn.domain.repository

import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.domain.model.Mood

// domain/repository/AIEngineRepository.kt

interface AIEngineRepository {

    /**
     * 1. Decisión de Movimiento
     *
     * Le pide a la IA que decida el mejor movimiento para el Board actual.
     * La decisión debe estar influenciada por el Mood (su Epsilon, que controla la exploración vs. explotación).
     * @param board El estado actual del tablero.
     * @param currentMood El estado de ánimo que determina la estrategia (epsilon).
     * @return Pair<Int, Int>? Las coordenadas (fila, columna) del movimiento elegido, o null si no hay movimientos válidos.
     */
    suspend fun getNextMove(board: Board, currentMood: Mood): Pair<Int, Int>?

    /**
     * 2. Aprendizaje y Actualización de Memoria
     *
     * Actualiza la memoria de la IA (la tabla Q-Learning) después de que la partida finalice,
     * para que aprenda de los resultados (victoria, derrota, empate).
     * @param gameHistory Una lista de todos los estados del Board durante la partida.
     */
    suspend fun updateMemory(gameHistory: List<Board>)

    /**
     * 3. Gestión del Estado (Reseteo)
     *
     * Borra toda la memoria de Q-Values aprendidos por la IA.
     */
    suspend fun clearMemory()

    /**
     * 4. Gestión del Estado (Carga inicial)
     * * Carga el estado de ánimo guardado para el día actual.
     * @return Mood El estado de ánimo actual guardado (o el predeterminado si no hay ninguno).
     */
    suspend fun getDailyMood(): Mood

    /**
     * 5. Gestión del Estado (Guardado)
     * * Guarda el estado de ánimo elegido por el usuario para el día actual.
     * @param mood El estado de ánimo seleccionado.
     */
    suspend fun saveDailyMood(mood: Mood)
}