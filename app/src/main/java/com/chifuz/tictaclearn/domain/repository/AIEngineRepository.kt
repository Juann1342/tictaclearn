// domain/repository/AIEngineRepository.kt
package com.chifuz.tictaclearn.domain.repository

import com.chifuz.tictaclearn.domain.model.Board
import com.chifuz.tictaclearn.domain.model.Mood

interface AIEngineRepository {

    suspend fun getNextMove(board: Board, currentMood: Mood): Int?

    suspend fun updateMemory(gameHistory: List<Board>)

    suspend fun getDailyMood(): Mood

    suspend fun saveDailyMood(mood: Mood)

    suspend fun clearMemory()

    suspend fun getClassicGamesPlayedCount(): Int
}