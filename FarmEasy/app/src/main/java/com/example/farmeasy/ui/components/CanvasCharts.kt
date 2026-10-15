package com.example.farmeasy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmeasy.domain.model.ForecastItem
import com.example.farmeasy.domain.model.SoilParameters
import com.example.farmeasy.ui.theme.ForestGreenPrimary
import com.example.farmeasy.ui.theme.StatusError
import com.example.farmeasy.ui.theme.StatusSuccess
import com.example.farmeasy.ui.theme.StatusWarning

@Composable
fun WeatherTrendChart(
    forecast: List<ForecastItem>,
    modifier: Modifier = Modifier
) {
    if (forecast.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "7-Day Temperature Trend (°C)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val width = size.width
            val height = size.height
            val count = forecast.size
            if (count < 2) return@Canvas

            val stepX = width / (count - 1)
            val maxTempOverall = forecast.maxOfOrNull { it.maxTemp } ?: 40.0
            val minTempOverall = forecast.minOfOrNull { it.minTemp } ?: 15.0
            val range = (maxTempOverall - minTempOverall).coerceAtLeast(5.0)

            val maxPath = Path()
            val minPath = Path()

            forecast.forEachIndexed { i, item ->
                val x = i * stepX
                val yMax = (height * 0.2f) + ((maxTempOverall - item.maxTemp) / range * (height * 0.5f)).toFloat()
                val yMin = (height * 0.2f) + ((maxTempOverall - item.minTemp) / range * (height * 0.5f)).toFloat()

                if (i == 0) {
                    maxPath.moveTo(x, yMax)
                    minPath.moveTo(x, yMin)
                } else {
                    maxPath.lineTo(x, yMax)
                    minPath.lineTo(x, yMin)
                }

                // Draw dots
                drawCircle(
                    color = Color(0xFFE65100),
                    radius = 4.dp.toPx(),
                    center = Offset(x, yMax)
                )
                drawCircle(
                    color = Color(0xFF1976D2),
                    radius = 4.dp.toPx(),
                    center = Offset(x, yMin)
                )

                // Day labels
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 28f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawText(item.dayOfWeek, x, height - 10f, paint)
                    drawText("${item.maxTemp.toInt()}°", x, yMax - 14f, paint)
                }
            }

            // Draw line connecting temperatures
            drawPath(
                path = maxPath,
                color = Color(0xFFE65100),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = minPath,
                color = Color(0xFF1976D2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun SoilNpkGauge(
    score: Int,
    parameters: SoilParameters,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(160.dp)
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val strokeWidth = 14.dp.toPx()
                val sweep = 240f
                val start = 150f

                // Track Arc
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Value Arc
                val progressSweep = (score / 100f).coerceIn(0f, 1f) * sweep
                val gaugeColor = when {
                    score >= 70 -> StatusSuccess
                    score >= 45 -> StatusWarning
                    else -> StatusError
                }
                drawArc(
                    color = gaugeColor,
                    startAngle = start,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Fertility Index",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // N, P, K progress bars
        NpkBarItem(label = "Nitrogen (N)", value = "${parameters.nitrogen} kg/ha", progress = (parameters.nitrogen / 350.0).toFloat().coerceIn(0f, 1f), color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(8.dp))
        NpkBarItem(label = "Phosphorus (P)", value = "${parameters.phosphorus} kg/ha", progress = (parameters.phosphorus / 70.0).toFloat().coerceIn(0f, 1f), color = Color(0xFFE65100))
        Spacer(modifier = Modifier.height(8.dp))
        NpkBarItem(label = "Potassium (K)", value = "${parameters.potassium} kg/ha", progress = (parameters.potassium / 400.0).toFloat().coerceIn(0f, 1f), color = Color(0xFF1565C0))
        Spacer(modifier = Modifier.height(8.dp))
        NpkBarItem(label = "Soil pH", value = "${parameters.ph}", progress = ((parameters.ph - 4.0) / 6.0).toFloat().coerceIn(0f, 1f), color = Color(0xFF6A1B9A))
    }
}

@Composable
private fun NpkBarItem(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(text = value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        ) {
            drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.3f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
            drawRoundRect(
                color = color,
                size = Size(size.width * progress, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )
        }
    }
}
