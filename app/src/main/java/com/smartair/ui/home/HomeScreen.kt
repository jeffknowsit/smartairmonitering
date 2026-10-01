package com.smartair.ui.home

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartair.data.model.*
import com.smartair.core.status.StatusEngine
import com.smartair.ui.components.*
import com.smartair.ui.theme.SmartAirColors

/**
 * Home screen - Command Center.
 * Faithfully reproduces the Stitch design with live data bindings.
 */
@Composable
fun HomeScreen(
    state: DashboardState,
    mockScenario: MockScenario,
    onMockScenarioChange: (MockScenario) -> Unit,
    onAskAi: () -> Unit,
    onRefreshInsight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        // Developer mock mode pills
        if (state.isMockMode) {
            MockModePills(
                selected = mockScenario,
                onSelect = onMockScenarioChange
            )
        }

        // Hero Air Status Card
        HeroStatusCard(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        // OTG Disconnect Banner
        if (state.connectionState == ConnectionState.DISCONNECTED ||
            state.connectionState == ConnectionState.ERROR) {
            OtgDisconnectBanner(state = state.connectionState)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Live Environmental Telemetry Grid
        MetricGridSection(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        // Ventilation Strip
        VentilationStrip(
            fanActive = state.currentReading.fan,
            buzzerActive = state.currentReading.buzzer
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4-Hour Trend Sparklines
        TrendSparklineSection(state = state)

        Spacer(modifier = Modifier.height(16.dp))

        // AI Smart Insight Card
        AiInsightCard(
            insight = state.aiInsight,
            onAskAi = onAskAi,
            onRefresh = onRefreshInsight
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MockModePills(
    selected: MockScenario,
    onSelect: (MockScenario) -> Unit
) {
    val scenarios = listOf(
        MockScenario.NORMAL,
        MockScenario.WARNING_DUST,
        MockScenario.CRITICAL,
        MockScenario.DISCONNECTED
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Tune,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = SmartAirColors.OnSurfaceVariant
        )
        Text(
            text = "Mode:",
            style = MaterialTheme.typography.labelLarge,
            color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.7f)
        )
        scenarios.forEach { scenario ->
            val isSelected = scenario == selected
            Surface(
                onClick = { onSelect(scenario) },
                shape = CircleShape,
                color = if (isSelected) SmartAirColors.Primary else SmartAirColors.SurfaceContainerHigh,
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Text(
                    text = scenario.label,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (isSelected) Color.White else SmartAirColors.OnSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun HeroStatusCard(state: DashboardState) {
    val (glowColor, coreGradient, kickerColor) = when (state.airStatus) {
        AirStatus.NORMAL -> Triple(
            SmartAirColors.SecondaryContainer.copy(alpha = 0.4f),
            Brush.linearGradient(listOf(SmartAirColors.Primary, SmartAirColors.PrimaryContainer)),
            SmartAirColors.Primary
        )
        AirStatus.WARNING -> Triple(
            SmartAirColors.SurfaceVariant.copy(alpha = 0.6f),
            Brush.linearGradient(listOf(SmartAirColors.PrimaryContainer, SmartAirColors.TertiaryContainer)),
            SmartAirColors.PrimaryContainer
        )
        AirStatus.CRITICAL -> Triple(
            SmartAirColors.ErrorContainer.copy(alpha = 0.4f),
            Brush.linearGradient(listOf(SmartAirColors.Error, SmartAirColors.ErrorContainer)),
            SmartAirColors.Error
        )
    }

    // Breathing animation
    val breathScale by rememberInfiniteTransition(label = "breath").animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    val outerPulseAlpha by rememberInfiniteTransition(label = "outerPulse").animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseOut),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerAlpha"
    )

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            // Ambient glow
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 56.dp, y = (-56).dp)
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(glowColor)
                    .blur(100.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Status text
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(kickerColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.statusKicker.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = kickerColor,
                            letterSpacing = 0.08.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.statusHeadline,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = SmartAirColors.OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.statusSubtext,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SmartAirColors.OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Quick telemetry pills
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TelemetryPill(
                            icon = Icons.Outlined.Thermostat,
                            value = state.currentReading.temperature?.let { "%.1f°C".format(it) } ?: "--"
                        )
                        TelemetryPill(
                            icon = Icons.Outlined.WaterDrop,
                            value = state.currentReading.humidity?.let { "%.0f%%".format(it) } ?: "--"
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Right: Breathing orb
                Box(
                    modifier = Modifier.size(112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer ripple
                    if (state.connectionState == ConnectionState.CONNECTED) {
                        Box(
                            modifier = Modifier
                                .size(112.dp)
                                .scale(breathScale * 1.1f)
                                .alpha(outerPulseAlpha)
                                .clip(CircleShape)
                                .background(kickerColor.copy(alpha = 0.1f))
                        )
                    }
                    // Middle halo
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .scale(breathScale)
                            .clip(CircleShape)
                            .background(kickerColor.copy(alpha = 0.15f))
                    )
                    // Core orb
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(coreGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val icon = when (state.airStatus) {
                                AirStatus.NORMAL -> Icons.Outlined.Air
                                AirStatus.WARNING -> Icons.Outlined.Grain
                                AirStatus.CRITICAL -> Icons.Outlined.Warning
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = when (state.airStatus) {
                                    AirStatus.NORMAL -> "Optimal"
                                    AirStatus.WARNING -> "Elevated"
                                    AirStatus.CRITICAL -> "Critical"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.08.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Surface(
        shape = CircleShape,
        color = SmartAirColors.SurfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = SmartAirColors.Primary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = SmartAirColors.OnSurface
            )
        }
    }
}

@Composable
private fun OtgDisconnectBanner(state: ConnectionState) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SmartAirColors.SurfaceContainerHighest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = SmartAirColors.Error,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.UsbOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (state == ConnectionState.ERROR) "Connection Error"
                               else "USB OTG Disconnected",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = SmartAirColors.OnSurface
                    )
                    Text(
                        text = "HALT",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 10.sp),
                        color = SmartAirColors.Error
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect Arduino sensor board via OTG cable to resume live telemetry.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartAirColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricGridSection(state: DashboardState) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Live Environmental Telemetry",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SmartAirColors.OnSurface
            )
            Text(
                text = "4 sensors active",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SmartAirColors.OnSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        val reading = state.currentReading
        val sensorStatus = state.airStatus

        // 2x2 Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                label = "Temperature",
                value = reading.temperature?.let { "%.1f".format(it) } ?: "--",
                unit = "°C",
                description = when {
                    reading.temperature == null -> "No feed"
                    reading.temperature!! > 35 -> "Hot"
                    reading.temperature!! > 30 -> "Warm"
                    else -> "Normal"
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.DeviceThermostat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                status = sensorStatus,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Humidity",
                value = reading.humidity?.let { "%.0f".format(it) } ?: "--",
                unit = "%",
                description = when {
                    reading.humidity == null -> "No feed"
                    reading.humidity!! > 75 -> "Muggy"
                    reading.humidity!! > 60 -> "Humid"
                    else -> "Comfortable"
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                status = sensorStatus,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricTile(
                label = "Dust",
                value = reading.dust?.toString() ?: "---",
                unit = "idx",
                description = when {
                    reading.dust == null -> "Sensor lost"
                    reading.dust!! > 400 -> "Severe Spike"
                    reading.dust!! > 270 -> "Elevated"
                    else -> "Sensor reading"
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.BlurOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                status = sensorStatus,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                label = "Gas",
                value = reading.gas?.toString() ?: "---",
                unit = "raw",
                description = when {
                    reading.gas == null -> "MQ-5 lost"
                    reading.gas!! > 400 -> "MQ-5 Detected"
                    else -> "MQ-5"
                },
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                status = sensorStatus,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun VentilationStrip(
    fanActive: Boolean,
    buzzerActive: Boolean
) {
    val fanRotation by rememberInfiniteTransition(label = "fan").animateFloat(
        initialValue = 0f,
        targetValue = if (fanActive) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        ),
        label = "fanRotation"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Fan icon container
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SmartAirColors.SecondaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Outlined.Air,
                            contentDescription = null,
                            tint = SmartAirColors.Primary,
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(if (fanActive) fanRotation else 0f)
                        )
                    }
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Ventilation",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = SmartAirColors.OnSurface
                        )
                        Surface(
                            shape = CircleShape,
                            color = if (fanActive) SmartAirColors.SecondaryContainer
                                    else SmartAirColors.SurfaceContainer
                        ) {
                            Text(
                                text = if (fanActive) "ON" else "STANDBY",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (fanActive) SmartAirColors.OnSecondaryContainer
                                        else SmartAirColors.OnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(
                        text = if (fanActive) "Improving room circulation"
                               else "Circulation off",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartAirColors.OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Buzzer indicator (if active)
            if (buzzerActive) {
                Surface(
                    shape = CircleShape,
                    color = SmartAirColors.ErrorContainer,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Campaign,
                            contentDescription = null,
                            tint = SmartAirColors.Error,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "ALARM",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SmartAirColors.Error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendSparklineSection(state: DashboardState) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "4-Hour Dynamics",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = SmartAirColors.OnSurface
            )
            Text(
                text = "Live Sparklines",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SmartAirColors.OnSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SparklineTrendCard(
                label = "Temperature",
                value = state.currentReading.temperature?.let { "%.1f°C".format(it) } ?: "--",
                trendText = StatusEngine.getTrendDescription(state.recentTemps),
                data = state.recentTemps,
                lineColor = SmartAirColors.Primary,
                modifier = Modifier.weight(1f)
            )
            SparklineTrendCard(
                label = "Humidity",
                value = state.currentReading.humidity?.let { "%.0f%%".format(it) } ?: "--",
                trendText = StatusEngine.getTrendDescription(state.recentHumidity),
                data = state.recentHumidity,
                lineColor = SmartAirColors.TertiaryContainer,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SparklineTrendCard(
                label = "Dust",
                value = state.currentReading.dust?.toString() ?: "--",
                trendText = StatusEngine.getTrendDescription(state.recentDust),
                data = state.recentDust,
                lineColor = SmartAirColors.PrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            SparklineTrendCard(
                label = "MQ-5",
                value = state.currentReading.gas?.toString() ?: "--",
                trendText = StatusEngine.getTrendDescription(state.recentGas),
                data = state.recentGas,
                lineColor = SmartAirColors.Secondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AiInsightCard(
    insight: String,
    onAskAi: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SmartAirColors.SurfaceContainerLowest,
                            SmartAirColors.SurfaceContainerLow
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            // Glow accent
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .size(112.dp)
                    .clip(CircleShape)
                    .background(SmartAirColors.SecondaryContainer.copy(alpha = 0.3f))
                    .blur(60.dp)
            )

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "✦",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
                            color = SmartAirColors.Primary
                        )
                        Text(
                            text = "Smart insight",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SmartAirColors.Primary
                        )
                    }
                    TextButton(
                        onClick = onAskAi,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        Text(
                            text = "Ask AI →",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SmartAirColors.Primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = insight,
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartAirColors.OnSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
