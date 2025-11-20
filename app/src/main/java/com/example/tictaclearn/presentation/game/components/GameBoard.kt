package com.example.tictaclearn.presentation.game.components

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tictaclearn.domain.model.Board
import com.example.tictaclearn.ui.theme.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.material3.LocalTextStyle
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

// 🚨 IMPORTANTE: Asegúrate de tener este color definido en tu archivo 'theme' (ui.theme.kt)
// Si no lo tienes, puedes añadirlo ahí o usar esta definición temporal.
val NeonGreen = Color(0xFF20C997) // Verde neón para el fondo/cuadrícula


@Composable
fun GameBoard(
    board: Board,
    onCellClicked: (Int) -> Unit,
    isProcessing: Boolean,
    winningCells: List<Int>,
    modifier: Modifier = Modifier
) {
    val columns = board.sideSize
    // Ajuste de espaciado basado en el tamaño del tablero
    val spacing: Dp = if (columns > 3) 2.dp else 8.dp

    Card(
        shape = RoundedCornerShape(16.dp),
        // 🚨 CAMBIO UI: Color de fondo más oscuro para el neón
        colors = CardDefaults.cardColors(containerColor = BackgroundDark),
        // Borde de tarjeta sutil para el tablero
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .aspectRatio(1f)
            // 🚨 CAMBIO UI: Borde de neón verde tenue para el tablero
            .border(2.dp, NeonGreen.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .graphicsLayer {
                // Aquí podrías añadir optimizaciones o efectos a nivel de capa
            }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // 🚨 CAMBIO UI: Fondo de cuadrícula neón verde
            Canvas(modifier = Modifier.matchParentSize()) {
                val spacingPx = spacing.toPx()
                // Calcular el tamaño de la celda, asumiendo que el padding general es 'spacing'
                val cellSize = (size.width - (columns + 1) * spacingPx) / columns

                // Dibujar el patrón de fondo de la cuadrícula
                for (i in 1 until columns) {
                    val x = spacingPx + i * (cellSize + spacingPx)

                    val y = spacingPx + i * (cellSize + spacingPx)
                    // Líneas horizontales

                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                horizontalArrangement = Arrangement.spacedBy(spacing),
                verticalArrangement = Arrangement.spacedBy(spacing),
                userScrollEnabled = false,
                // 🚨 CAMBIO UI: Un padding para que las celdas no toquen el borde de la Card
                modifier = Modifier.fillMaxSize().padding(spacing)
            ) {
                items(board.cells.size) { index ->
                    val isWinning = winningCells.contains(index)
                    BoardCell(
                        cellContent = board.cells[index],
                        onClick = {
                            if (!isProcessing && board.cells[index] == ' ') {
                                onCellClicked(index)
                            }
                        },
                        // El fontSize ya no se usa, pero se elimina del constructor por limpieza
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
        else -> Color.Transparent
    }

    // 🚨 CAMBIO UI: El fondo de la celda es ahora el color oscuro base
    val cellBackgroundColor = BackgroundDark.copy(alpha = 0.8f)

    // Parámetros para el estilo Neón/Contorno
    val iconSizeFactor = 0.35f
    val strokeWidth = 18f
    val glowRadius = if (isWinning) 35f else 40f
    val shadowColor = baseColor.copy(alpha = 1f)

    // 🚨 CAMBIO UI: Borde de la celda muy sutil para separarlas.
    val cellBorderColor = NeonGreen.copy(alpha = 0.5f) // Neón verde muy tenue


    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(cellBackgroundColor)
            .border(1.dp, cellBorderColor, RoundedCornerShape(6.dp))
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
                    // 🚨 CAMBIO CLAVE: Estilo STROKE para contorno hueco (Neón)
                    paint.style = android.graphics.Paint.Style.STROKE
                    paint.strokeWidth = strokeWidth
                    paint.strokeCap = android.graphics.Paint.Cap.ROUND

                    // Efecto Glow (Sombra)
                    paint.setShadowLayer(
                        glowRadius,
                        0f, 0f,
                        shadowColor.toArgb()
                    )

                    // Dibujar la Ficha
                    if (cellContent == 'O') {
                        // Dibujar el Círculo Hueco
                        it.nativeCanvas.drawCircle(
                            center.x, center.y, radius, paint
                        )
                    } else if (cellContent == 'X') {
                        // Dibujar la 'X' (dos líneas rotadas)
                        // 🚨 AJUSTE CLAVE: Uso consistente de STROKE y GLOW para que la 'X' se vea como la 'O'
                        val angle = 0.785398f // 45 grados

                        // Línea 1
                        it.nativeCanvas.drawLine(
                            center.x - radius * cos(angle.toDouble()).toFloat(),
                            center.y - radius * sin(angle.toDouble()).toFloat(),
                            center.x + radius * cos(angle.toDouble()).toFloat(),
                            center.y + radius * sin(angle.toDouble()).toFloat(),
                            paint
                        )
                        // Línea 2
                        it.nativeCanvas.drawLine(
                            center.x + radius * cos(angle.toDouble()).toFloat(),
                            center.y - radius * sin(angle.toDouble()).toFloat(),
                            center.x - radius * cos(angle.toDouble()).toFloat(),
                            center.y + radius * sin(angle.toDouble()).toFloat(),
                            paint
                        )
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