package com.chifuz.tictaclearn.presentation.game.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.chifuz.tictaclearn.domain.model.Mood

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameTopBar(currentMood: Mood?) {
    TopAppBar(
        title = {
            Text(
                text = "VS: ${currentMood?.displayName ?: "Cargando..."}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    )
}