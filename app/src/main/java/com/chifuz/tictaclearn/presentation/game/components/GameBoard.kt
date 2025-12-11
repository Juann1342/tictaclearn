
package com.chifuz.tictaclearn.presentation.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.chifuz.tictaclearn.domain.model.Board
import com.chifuz.tictaclearn.ui.theme.BackgroundDark
import com.chifuz.tictaclearn.ui.theme.NeonCyan
import com.chifuz.tictaclearn.ui.theme.NeonOrange
import kotlin.math.cos
import kotlin.math.sin
import kotlin.text.get

// Colores extra para Party Mode
val NeonGreen = Color(0xFF20C997)
val NeonPurple = Color(0xFFD946EF) // Para Triángulo
val NeonYellow = Color(0xFFFFD700) // Para Estrella

@Composable
fun GameBoard(
    board: Board,
    onCellClicked: (Int) -> Unit,
    isProcessing: Boolean,
    winningCells: List<Int>,
    modifier: Modifier = Modifier
) {
    val columns = board.sideSize
    val spacing: Dp = if (columns > 3) 2.dp else 8.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .aspectRatio(1f)
            .border(2.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Fondo de cuadrícula (Canvas simple)
            Canvas(modifier = Modifier.matchParentSize()) {
                val spacingPx = spacing.toPx()
                val cellSize = (size.width - (columns + 1) * spacingPx) / columns
                // Aquí iría el dibujo de líneas de fondo si lo deseas...
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize().padding(spacing)
            ) {
                items(board.cells.size) { index ->
                    val isWinning = winningCells.contains(index)
                    BoardCell(
                        cellContent = board.cells[index],
                        onClick = { if (!isProcessing && board.cells[index] == ' ') onCellClicked(index) },
                        isWinning = isWinning,
                        isGameOver = board.isFull || winningCells.isNotEmpty()
                    )
                }
            }
            if (winningCells.isNotEmpty() && winningCells.size > 1) {
                // Se pasa el tamaño de la línea de victoria para el Canvas
                WinningLineOverlay(
                    columns, winningCells, board.cells[winningCells.first()] == 'X',
                    spacing = spacing
                )
            }
        }
    }
}

@Composable
fun BoardCell(
    cellContent: Char,
    onClick: () -> Unit,
    isWinning: Boolean,
    isGameOver: Boolean
) {
    val baseColor = when (cellContent) {
        'X' -> NeonOrange
        'O' -> NeonCyan
        '△' -> NeonPurple
        '☆' -> NeonYellow
        else -> Color.Transparent
    }

    val cellBackgroundColor = BackgroundDark.copy(alpha = 0.8f)
    val iconSizeFactor = 0.35f
    val strokeWidth = 14f // Un poco más fino para 9x9
    val glowRadius = if (isWinning) 35f else 25f
    val shadowColor = baseColor.copy(alpha = 1f)
    val cellBorderColor = NeonGreen.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(cellBackgroundColor)
            .border(1.dp, cellBorderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick, enabled = !isGameOver && cellContent == ' '),
        contentAlignment = Alignment.Center
    ) {
        if (cellContent != ' ') {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width * iconSizeFactor

                drawIntoCanvas {
                    val paint = Paint().asFrameworkPaint()
                    paint.color = baseColor.toArgb()
                    paint.style = android.graphics.Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth
                    paint.strokeCap = android.graphics.Paint.Cap.ROUND
                    paint.setShadowLayer(glowRadius, 0f, 0f, shadowColor.toArgb())

                    when (cellContent) {
                        'O' -> it.nativeCanvas.drawCircle(center.x, center.y, radius, paint)
                        'X' -> {
                            val angle = 0.785398f
                            it.nativeCanvas.drawLine(
                                center.x - radius * cos(angle.toDouble()).toFloat(),
                                center.y - radius * sin(angle.toDouble()).toFloat(),
                                center.x + radius * cos(angle.toDouble()).toFloat(),
                                center.y + radius * sin(angle.toDouble()).toFloat(),
                                paint
                            )
                            it.nativeCanvas.drawLine(
                                center.x + radius * cos(angle.toDouble()).toFloat(),
                                center.y - radius * sin(angle.toDouble()).toFloat(),
                                center.x - radius * cos(angle.toDouble()).toFloat(),
                                center.y + radius * sin(angle.toDouble()).toFloat(),
                                paint
                            )
                        }
                        '△' -> {
                            // Dibujar Triángulo
                            val path = Path()
                            // Punta arriba
                            path.moveTo(center.x, center.y - radius)
                            // Abajo derecha
                            path.lineTo(center.x + radius * 0.866f, center.y + radius * 0.5f)
                            // Abajo izquierda
                            path.lineTo(center.x - radius * 0.866f, center.y + radius * 0.5f)
                            path.close()
                            it.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                        }
                        '☆' -> {
                            // Dibujar Estrella (simplificada)
                            val path = Path()
                            val outerRadius = radius
                            val innerRadius = radius * 0.4f
                            for (i in 0 until 5) {
                                val angleOuter = Math.toRadians((i * 72 - 90).toDouble())
                                val angleInner = Math.toRadians((i * 72 + 36 - 90).toDouble())
                                val xOuter = (center.x + outerRadius * cos(angleOuter)).toFloat()
                                val yOuter = (center.y + outerRadius * sin(angleOuter)).toFloat()
                                val xInner = (center.x + innerRadius * cos(angleInner)).toFloat()
                                val yInner = (center.y + innerRadius * sin(angleInner)).toFloat()
                                if (i == 0) path.moveTo(xOuter, yOuter) else path.lineTo(xOuter, yOuter)
                                path.lineTo(xInner, yInner)
                            }
                            path.close()
                            it.nativeCanvas.drawPath(path.asAndroidPath(), paint)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun WinningLineOverlay(columns: Int, winningCells: List<Int>, isXWin: Boolean, spacing: Dp) {
    val startCellIndex = winningCells.first()
    val endCellIndex = winningCells.last()

    val startRow = startCellIndex / columns
    val startCol = startCellIndex % columns
    val endRow = endCellIndex / columns
    val endCol = endCellIndex % columns

    val lineColor = if (isXWin) NeonOrange else NeonCyan
    val strokeWidth = if (columns > 3) 12f else 30f

    Canvas(modifier = Modifier.fillMaxSize()) {

        val spacingPx = spacing.toPx()

        // 1. Calcular el ancho total disponible para el GRID (Ancho de la Card - 2 * padding(spacing))
        // NOTA: El Canvas ocupa todo el Box, y el Box contiene el LazyVerticalGrid con padding(spacing).
        // Si el Box no tiene padding exterior, el ancho disponible es size.width

        // 🚨 CÁLCULO CORREGIDO: Se debe restar el padding exterior (2 * spacingPx)
        // y luego el espaciado interno (columns - 1) * spacingPx.
        val totalSpacingUsed = (2 * spacingPx) + ((columns - 1) * spacingPx)

        // El ancho neto disponible para el contenido (las celdas)
        val contentWidth = size.width - totalSpacingUsed

        // 🚨 Nueva fórmula de cellSize:
        val cellSize = contentWidth / columns
        val centerOffset = cellSize / 2f

        // El offset de inicio es el padding(spacing) de la LazyVerticalGrid
        val startOffset = spacingPx

        // 3. Recalcular las coordenadas del centro
        // Centro = (Offset de Inicio) + (Índice * (Tamaño Celda + Espacio)) + Mitad de Celda

        val startCenter = Offset(
            x = startOffset + (startCol * (cellSize + spacingPx)) + centerOffset,
            y = startOffset + (startRow * (cellSize + spacingPx)) + centerOffset
        )
        val endCenter = Offset(
            x = startOffset + (endCol * (cellSize + spacingPx)) + centerOffset,
            y = startOffset + (endRow * (cellSize + spacingPx)) + centerOffset
        )

        drawIntoCanvas {
            val paint = Paint().asFrameworkPaint()
            paint.color = lineColor.toArgb()
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = strokeWidth
            paint.strokeCap = android.graphics.Paint.Cap.ROUND

            // Efecto Glow
            paint.setShadowLayer(
                strokeWidth * 0.9f,
                0f, 0f,
                lineColor.copy(alpha = 0.9f).toArgb()
            )

            it.nativeCanvas.drawLine(startCenter.x, startCenter.y, endCenter.x, endCenter.y, paint)
        }
    }
}

