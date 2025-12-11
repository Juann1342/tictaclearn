package com.chifuz.tictaclearn.presentation.game.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.chifuz.tictaclearn.ui.theme.NeonCyan
import com.chifuz.tictaclearn.ui.theme.NeonRed
import androidx.compose.ui.unit.sp
import com.chifuz.tictaclearn.ui.theme.TextWhite
import androidx.compose.ui.unit.dp
import com.chifuz.tictaclearn.R
import com.chifuz.tictaclearn.domain.model.GameMode
import com.chifuz.tictaclearn.domain.model.GameResult
import com.chifuz.tictaclearn.domain.model.GameState
import com.chifuz.tictaclearn.domain.model.Player
import com.chifuz.tictaclearn.ui.theme.NeonPurple
import com.chifuz.tictaclearn.ui.theme.NeonYellow
import com.chifuz.tictaclearn.ui.theme.NeonOrange

@Composable
fun GameStatusIndicator(
    gameState: GameState,
    isAiThinking: Boolean,
    currentGameMode: GameMode,
    modifier: Modifier = Modifier
) {
    val statusText = when (gameState.result) {
        GameResult.Playing -> {
            if (gameState.currentPlayer == Player.AI && isAiThinking) {
                stringResource(R.string.status_ai_thinking)
            } else {
                when (currentGameMode) {
                    GameMode.CLASSIC, GameMode.GOMOKU -> {
                        when (gameState.currentPlayer) {
                            Player.Human -> stringResource(R.string.status_turn_human)
                            Player.AI -> stringResource(R.string.status_turn_ai)
                            else -> stringResource(R.string.status_turn_generic, gameState.currentPlayer.symbol)
                        }
                    }
                    GameMode.PARTY -> {
                        stringResource(R.string.status_turn_generic, gameState.currentPlayer.symbol)
                    }
                    else -> stringResource(R.string.loading)
                }
            }
        }
        GameResult.Draw -> stringResource(R.string.status_draw)
        is GameResult.Win -> {
            val winnerSymbol = gameState.result.winner.symbol
            stringResource(R.string.status_winner, winnerSymbol)
        }
    }

    val textColor = when (gameState.currentPlayer) {
        Player.Human -> NeonOrange
        Player.AI -> NeonCyan
        Player.Triangle -> NeonPurple
        Player.Star -> NeonYellow
    }

    Column(
        modifier = modifier.padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Black
            ),
            color = when (gameState.result) {
                GameResult.Playing -> textColor
                GameResult.Draw -> TextWhite
                is GameResult.Win -> {
                    when (gameState.result.winner) {
                        Player.Human -> NeonOrange
                        Player.AI -> NeonCyan
                        Player.Triangle -> NeonPurple
                        Player.Star -> NeonYellow
                    }
                }
            }
        )

        if (gameState.result == GameResult.Playing) {
            val (markerText, markerColor) = when (gameState.currentPlayer) {
                Player.Human -> "(${Player.Human.symbol})" to NeonOrange
                Player.AI -> "(${Player.AI.symbol})" to NeonCyan
                Player.Triangle -> "(${Player.Triangle.symbol})" to NeonPurple
                Player.Star -> "(${Player.Star.symbol})" to NeonYellow
            }

            Text(
                text = markerText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = markerColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        } else if (gameState.result is GameResult.Win && gameState.result.winner == Player.AI) {
            Text(
                text = stringResource(R.string.status_learn_msg),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = NeonRed.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}