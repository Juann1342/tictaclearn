package com.chifuz.tictaclearn.presentation.game.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.chifuz.tictaclearn.R
import com.chifuz.tictaclearn.domain.model.Mood
import com.chifuz.tictaclearn.domain.model.GameMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(
    currentMood: Mood?,
    currentGameMode: GameMode
) {
    TopAppBar(
        title = {
            val titleText = if (currentGameMode == GameMode.PARTY) {
                stringResource(R.string.party_mode_title)
            } else {
                val moodName = currentMood?.displayNameRes?.let { stringResource(it) } ?: stringResource(R.string.loading)
                stringResource(R.string.vs_prefix, moodName)
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}