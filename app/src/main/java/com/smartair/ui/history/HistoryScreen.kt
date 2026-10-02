package com.smartair.ui.history

import androidx.compose.foundation.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartair.data.model.*
import com.smartair.ui.theme.SmartAirColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * History screen - Analytics.
 * Faithfully reproduces the Stitch History design.
 */
@Composable
fun HistoryScreen(
    state: HistoryState,
    onRangeSelected: (TimeRange) -> Unit,
    onMetricSelected: (MetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "TELEMETRY LOGS",
                    style = MaterialTheme.typography.labelLarge,
                    color = SmartAirColors.Primary,
                    letterSpacing = 0.08.sp
                )
                Text(
                    text = "History",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SmartAirColors.OnSurface
                )
                Text(
                    text = "Sensor Trends",
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartAirColors.OnSurfaceVariant
                )
            }
            // Time Range Pills
            TimeRangePills(
                selected = state.selectedRange,
                onSelect = onRangeSelected
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Metric Filter Chips
        MetricFilterChips(
            selected = state.selectedMetric,
            onSelect = onMetricSelected
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Main Chart Card
        ChartCard(state = state, timeFormat = timeFormat)

        Spacer(modifier = Modifier.height(12.dp))

        // Stats Grid
        StatsGrid(state = state, timeFormat = timeFormat)

        Spacer(modifier = Modifier.height(12.dp))

        // Ventilation Impact Banner
        VentilationImpactBanner()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TimeRangePills(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit
) {
    Surface(
        shape = CircleShape,
        color = SmartAirColors.SurfaceContainerHigh,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            TimeRange.values().forEach { range ->
                val isSelected = range == selected
                Surface(
                    onClick = { onSelect(range) },
                    shape = CircleShape,
                    color = if (isSelected) SmartAirColors.SurfaceContainerLowest
                            else Color.Transparent,
                    shadowElevation = if (isSelected) 1.dp else 0.dp
                ) {
                    Text(
                        text = range.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) SmartAirColors.Primary
                                else SmartAirColors.OnSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricFilterChips(
    selected: MetricType,
    onSelect: (MetricType) -> Unit
) {
    val metrics = listOf(
        Triple(MetricType.TEMPERATURE, "Temperature", Icons.Outlined.DeviceThermostat),
        Triple(MetricType.HUMIDITY, "Humidity", Icons.Outlined.WaterDrop),
        Triple(MetricType.DUST, "Dust", Icons.Outlined.Grain),
        Triple(MetricType.GAS, "MQ-5", Icons.Outlined.Air)
    )

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        metrics.forEach { (type, label, icon) ->
            val isSelected = type == selected
            Surface(
                onClick = { onSelect(type) },
                shape = CircleShape,
                color = if (isSelected) SmartAirColors.Primary
                        else SmartAirColors.SurfaceContainerLowest,
                shadowElevation = if (isSelected) 2.dp else 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSelected) SmartAirColors.PrimaryFixed
                               else SmartAirColors.OnSurfaceVariant
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                        ),
                        color = if (isSelected) SmartAirColors.OnPrimary
                                else SmartAirColors.OnSurfaceVariant
                    )
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SmartAirColors.SecondaryFixed)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(state: HistoryState, timeFormat: SimpleDateFormat) {
    val metricLabel = when (state.selectedMetric) {
        MetricType.TEMPERATURE -> "Temperature"
        MetricType.HUMIDITY -> "Humidity"
        MetricType.DUST -> "Dust"
        MetricType.GAS -> "MQ-5 Gas"
    }
    val unitLabel = when (state.selectedMetric) {
        MetricType.TEMPERATURE -> "°C"
        MetricType.HUMIDITY -> "%"
        MetricType.DUST -> "idx"
        MetricType.GAS -> "raw"
    }

    val chartData = state.readings.mapNotNull { reading ->
        when (state.selectedMetric) {
            MetricType.TEMPERATURE -> reading.temperature
            MetricType.HUMIDITY -> reading.humidity
            MetricType.DUST -> reading.dust?.toFloat()
            MetricType.GAS -> reading.gas?.toFloat()
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Surface(
                        shape = CircleShape,
                        color = SmartAirColors.SecondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SmartAirColors.Primary)
                            )
                            Text(
                                text = "$metricLabel Trend — Last ${state.selectedRange.label}",
                                style = MaterialTheme.typography.labelLarge,
                                color = SmartAirColors.OnSecondaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (chartData.isNotEmpty()) "%.0f".format(chartData.last())
                                   else "--",
                            style = MaterialTheme.typography.titleLarge,
                            color = SmartAirColors.OnSurface
                        )
                        Text(
                            text = unitLabel,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = SmartAirColors.OnSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                if (chartData.size >= 2) {
                    val change = ((chartData.last() - chartData.first()) / chartData.first() * 100)
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (change < 0) Icons.Outlined.TrendingDown
                                              else Icons.Outlined.TrendingUp,
                                contentDescription = null,
                                tint = SmartAirColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "%.1f%%".format(change),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = SmartAirColors.Primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Chart area
            if (chartData.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Timeline,
                            contentDescription = null,
                            tint = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No data available for this period",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Data will appear as sensor readings are collected",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                // Area chart
                AreaChart(
                    data = chartData,
                    warningThreshold = when (state.selectedMetric) {
                        MetricType.DUST -> 270f
                        MetricType.GAS -> 400f
                        MetricType.TEMPERATURE -> 35f
                        MetricType.HUMIDITY -> 75f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(224.dp)
                )
            }
        }
    }
}

@Composable
private fun AreaChart(
    data: List<Float>,
    warningThreshold: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 8f

        if (data.size < 2) return@Canvas

        val minVal = data.min() * 0.9f
        val maxVal = data.max() * 1.1f
        val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

        val points = data.mapIndexed { index, value ->
            val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
            val y = padding + (1f - (value - minVal) / range) * (height - 2 * padding)
            Offset(x, y)
        }

        // Grid lines
        for (i in 0..2) {
            val y = padding + (height - 2 * padding) * i / 2f
            drawLine(
                color = Color(0xFFDAE2FD),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 0.75f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f))
            )
        }

        // Warning threshold line
        val thresholdY = padding + (1f - (warningThreshold - minVal) / range) * (height - 2 * padding)
        if (thresholdY in 0f..height) {
            drawLine(
                color = Color(0xFFBA1A1A).copy(alpha = 0.5f),
                start = Offset(0f, thresholdY),
                end = Offset(width, thresholdY),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))
            )
        }

        // Build curve path
        val curvePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val cpx = (prev.x + curr.x) / 2
                cubicTo(cpx, prev.y, cpx, curr.y, curr.x, curr.y)
            }
        }

        // Fill
        val fillPath = Path().apply {
            addPath(curvePath)
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF00685F).copy(alpha = 0.28f),
                    Color(0xFF00685F).copy(alpha = 0.04f),
                    Color(0xFF00685F).copy(alpha = 0f)
                )
            )
        )

        // Stroke
        drawPath(
            path = curvePath,
            color = Color(0xFF00685F),
            style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Current point dot
        val lastPoint = points.last()
        drawCircle(color = Color(0xFF00685F).copy(alpha = 0.2f), radius = 8f, center = lastPoint)
        drawCircle(color = Color.White, radius = 5f, center = lastPoint)
        drawCircle(color = Color(0xFF00685F), radius = 4f, center = lastPoint)
    }
}

@Composable
private fun StatsGrid(state: HistoryState, timeFormat: SimpleDateFormat) {
    val unitLabel = when (state.selectedMetric) {
        MetricType.TEMPERATURE -> "°C"
        MetricType.HUMIDITY -> "%"
        MetricType.DUST -> "idx"
        MetricType.GAS -> "raw"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Average
        StatCard(
            label = "Average",
            value = "%.0f".format(state.average),
            unit = unitLabel,
            detail = "Over ${state.selectedRange.label}",
            icon = Icons.Outlined.Speed,
            iconTint = SmartAirColors.Tertiary,
            modifier = Modifier.weight(1f)
        )
        // Peak
        StatCard(
            label = "Peak",
            value = "%.0f".format(state.peak),
            unit = unitLabel,
            detail = if (state.peakTime > 0) timeFormat.format(Date(state.peakTime))
                     else "No data",
            icon = Icons.Outlined.PriorityHigh,
            iconTint = SmartAirColors.Error,
            valueColor = SmartAirColors.Error,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Minimum
        StatCard(
            label = "Minimum",
            value = "%.0f".format(state.minimum),
            unit = unitLabel,
            detail = if (state.minimumTime > 0) timeFormat.format(Date(state.minimumTime))
                     else "No data",
            icon = Icons.Outlined.Eco,
            iconTint = SmartAirColors.Secondary,
            modifier = Modifier.weight(1f)
        )
        // Warnings
        StatCard(
            label = "Warnings",
            value = "${state.warningCount}",
            unit = "events",
            detail = "In ${state.selectedRange.label} window",
            icon = Icons.Outlined.WarningAmber,
            iconTint = SmartAirColors.OnSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    detail: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    valueColor: Color = SmartAirColors.OnSurface,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = SmartAirColors.OnSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = iconTint
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = valueColor
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = SmartAirColors.OnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = SmartAirColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun VentilationImpactBanner() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            SmartAirColors.SurfaceContainerLow,
                            SmartAirColors.SurfaceContainer,
                            SmartAirColors.SurfaceContainerLow
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SmartAirColors.PrimaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.Air,
                                contentDescription = null,
                                tint = SmartAirColors.OnPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Ventilation Impact",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SmartAirColors.OnSurface
                        )
                        Text(
                            text = "Fan activity tracked",
                            style = MaterialTheme.typography.bodySmall,
                            color = SmartAirColors.OnSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
