package com.chifuz.tictaclearn.presentation.configuration.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.chifuz.tictaclearn.R
import com.chifuz.tictaclearn.ui.theme.BackgroundDark
import com.chifuz.tictaclearn.ui.theme.NeonCyan
import com.chifuz.tictaclearn.ui.theme.NeonOrange
import com.chifuz.tictaclearn.ui.theme.SurfaceDark
import com.chifuz.tictaclearn.ui.theme.TextGray
import com.chifuz.tictaclearn.ui.theme.TextWhite

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    isSoundEnabled: Boolean,
    onSoundToggle: (Boolean) -> Unit,
    isVibrationEnabled: Boolean,
    onVibrationToggle: (Boolean) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = BackgroundDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonOrange,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Toggle Sonido
                SettingsRow(
                    label = stringResource(R.string.settings_sound),
                    isChecked = isSoundEnabled,
                    onCheckedChange = onSoundToggle
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Toggle Vibración
                SettingsRow(
                    label = stringResource(R.string.settings_vibration),
                    isChecked = isVibrationEnabled,
                    onCheckedChange = onVibrationToggle
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_close), color = TextWhite)
                }
            }
        }
    }
}

@Composable
fun SettingsRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextWhite,
            fontWeight = FontWeight.Bold
        )
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = NeonCyan,
                checkedTrackColor = NeonCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = TextGray,
                uncheckedTrackColor = SurfaceDark
            )
        )
    }
}