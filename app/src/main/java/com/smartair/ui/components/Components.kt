package com.smartair.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartair.data.model.AirStatus
import com.smartair.data.model.ConnectionState
import com.smartair.ui.theme.SmartAirColors

/**
 * Compact sparkline chart matching Stitch design.
 * Bezier curve with gradient fill and endpoint dot.
 */
@Composable
fun SparklineChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = SmartAirColors.Primary,
    fillAlpha: Float = 0.25f
) {
    if (data.size < 2) {
        // Empty state - draw a flat line
        Canvas(modifier = modifier) {
            val y = size.height * 0.6f
            drawLine(
                color = lineColor.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )
        }
        return
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 4f

        val minVal = data.min()
        val maxVal = data.max()
        val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

        val points = data.mapIndexed { index, value ->
            val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
            val y = padding + (1f - (value - minVal) / range) * (height - 2 * padding)
            Offset(x, y)
        }

        // Build path
        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cpx = (prev.x + curr.x) / 2
                cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
            }
        }

        // Fill path
        val fillPath = Path().apply {
            addPath(path)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = fillAlpha),
                    lineColor.copy(alpha = 0f)
                )
            )
        )

        // Stroke
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Endpoint dot
        val lastPoint = points.last()
        drawCircle(color = lineColor, radius = 4f, center = lastPoint)
    }
}

/**
 * Metric tile card matching Stitch 2x2 grid design.
 */
@Composable
fun MetricTile(
    label: String,
    value: String,
    unit: String,
    description: String,
    icon: @Composable () -> Unit,
    status: AirStatus = AirStatus.NORMAL,
    modifier: Modifier = Modifier
) {
    val pipColor = when (status) {
        AirStatus.NORMAL -> SmartAirColors.Primary
        AirStatus.WARNING -> SmartAirColors.StatusWarning
        AirStatus.CRITICAL -> SmartAirColors.StatusCritical
    }
    val descColor = when (status) {
        AirStatus.NORMAL -> SmartAirColors.Primary
        AirStatus.WARNING -> SmartAirColors.StatusWarning
        AirStatus.CRITICAL -> SmartAirColors.StatusCritical
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = SmartAirColors.OnSurfaceVariant,
                    letterSpacing = 0.08.sp
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(pipColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = (-0.02).sp
                    ),
                    color = SmartAirColors.OnSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = SmartAirColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = descColor
                )
                icon()
            }
        }
    }
}

/**
 * Sparkline trend card matching Stitch design.
 */
@Composable
fun SparklineTrendCard(
    label: String,
    value: String,
    trendText: String,
    data: List<Float>,
    lineColor: Color = SmartAirColors.Primary,
    modifier: Modifier = Modifier
) {
    val trendColor = when {
        trendText.startsWith("↑") -> SmartAirColors.Primary
        trendText.startsWith("↓") -> SmartAirColors.Primary
        else -> SmartAirColors.OnSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = SmartAirColors.OnSurfaceVariant,
                letterSpacing = 0.08.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = SmartAirColors.OnSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = trendText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = trendColor,
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SparklineChart(
                data = data,
                lineColor = lineColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )
        }
    }
}

/**
 * Connection status badge matching Stitch header.
 */
@Composable
fun ConnectionBadge(
    state: ConnectionState,
    modifier: Modifier = Modifier
) {
    val (text, color, showPulse) = when (state) {
        ConnectionState.CONNECTED -> Triple("Arduino Live", SmartAirColors.Primary, true)
        ConnectionState.CONNECTING -> Triple("Connecting...", SmartAirColors.Primary, true)
        ConnectionState.DISCONNECTED -> Triple("Disconnected", SmartAirColors.Outline, false)
        ConnectionState.PERMISSION_REQUIRED -> Triple("Permission Required", SmartAirColors.StatusWarning, false)
        ConnectionState.ERROR -> Triple("Error", SmartAirColors.Error, false)
    }

    val pulseAlpha by if (showPulse) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 0.75f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2500, easing = EaseOut),
                repeatMode = RepeatMode.Restart
            ),
            label = "pulseAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = SmartAirColors.SecondaryContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (showPulse) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = pulseAlpha))
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.08.sp
                ),
                color = color
            )
        }
    }
}

/**
 * Toggle switch matching Stitch design.
 */
@Composable
fun StitchToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = SmartAirColors.SurfaceContainerLowest,
            checkedTrackColor = SmartAirColors.Primary,
            uncheckedThumbColor = SmartAirColors.SurfaceContainerLowest,
            uncheckedTrackColor = SmartAirColors.SurfaceVariant,
            uncheckedBorderColor = Color.Transparent,
            checkedBorderColor = Color.Transparent
        )
    )
}
