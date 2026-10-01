package com.smartair.ui.ai

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartair.data.model.ChatMessage
import com.smartair.data.model.DashboardState
import com.smartair.ui.theme.SmartAirColors
import kotlinx.coroutines.launch

/**
 * AI Assistant screen.
 * Faithfully reproduces the Stitch AI design with live chat.
 */
@Composable
fun AiScreen(
    dashboardState: DashboardState,
    chatMessages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1 + 4) // +4 for header items
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            // Header
            item {
                AiHeader(onClearChat = onClearChat)
            }

            // Live Context Strip
            item {
                SensorContextStrip(state = dashboardState)
            }

            // Room Diagnosis Card
            item {
                RoomDiagnosisCard(state = dashboardState)
            }

            // Suggested Prompts
            item {
                SuggestedPrompts(onPromptClick = { prompt ->
                    onSendMessage(prompt)
                })
            }

            // Chat Messages
            items(chatMessages) { message ->
                if (message.isUser) {
                    UserBubble(message = message.content)
                } else {
                    AiBubble(message = message.content)
                }
            }
        }

        // Chat Input
        ChatInput(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                if (inputText.text.isNotBlank()) {
                    onSendMessage(inputText.text.trim())
                    inputText = TextFieldValue("")
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun AiHeader(onClearChat: () -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AI Assistant",
                    style = MaterialTheme.typography.headlineSmall,
                    color = SmartAirColors.OnSurface
                )
                // Neural badge
                val pulseScale by rememberInfiniteTransition(label = "neuralPulse").animateFloat(
                    initialValue = 1f,
                    targetValue = 0.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2500, easing = EaseInOut),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse"
                )
                Surface(
                    shape = CircleShape,
                    color = SmartAirColors.SecondaryContainer
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
                            text = "Neural",
                            style = MaterialTheme.typography.labelLarge,
                            color = SmartAirColors.OnSecondaryContainer
                        )
                    }
                }
            }
            // Clear button
            IconButton(
                onClick = onClearChat,
                modifier = Modifier
                    .size(32.dp)
                    .background(SmartAirColors.SurfaceContainerHigh, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Clear session",
                    modifier = Modifier.size(18.dp),
                    tint = SmartAirColors.OnSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Understand your room atmosphere & sensor dynamics",
            style = MaterialTheme.typography.bodyMedium,
            color = SmartAirColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun SensorContextStrip(state: DashboardState) {
    val reading = state.currentReading

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ContextChip(
            icon = Icons.Outlined.Thermostat,
            value = reading.temperature?.let { "%.1f°C".format(it) } ?: "--",
            label = when {
                reading.temperature == null -> "No data"
                reading.temperature!! > 30 -> "Warm"
                else -> "Normal"
            },
            iconTint = SmartAirColors.Primary
        )
        ContextChip(
            icon = Icons.Outlined.WaterDrop,
            value = reading.humidity?.let { "%.0f%%".format(it) } ?: "--",
            label = "Humidity",
            iconTint = SmartAirColors.PrimaryContainer
        )
        ContextChip(
            icon = Icons.Outlined.Grain,
            value = reading.dust?.let { "Dust $it" } ?: "Dust --",
            label = when {
                reading.dust == null -> "No data"
                reading.dust!! > 250 -> "Elevated"
                else -> "Normal"
            },
            iconTint = SmartAirColors.Secondary
        )
        ContextChip(
            icon = Icons.Outlined.Air,
            value = reading.gas?.let { "MQ-5 $it" } ?: "MQ-5 --",
            label = "Normal",
            iconTint = SmartAirColors.TertiaryContainer
        )
    }
}

@Composable
private fun ContextChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    iconTint: Color
) {
    Surface(
        shape = CircleShape,
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = iconTint
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = SmartAirColors.OnSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = SmartAirColors.OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun RoomDiagnosisCard(state: DashboardState) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            // Background glow
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 48.dp, y = (-48).dp)
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SmartAirColors.PrimaryFixedDim.copy(alpha = 0.3f),
                                SmartAirColors.SecondaryContainer.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
                    .blur(40.dp)
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SmartAirColors.PrimaryFixed,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Outlined.AutoAwesome,
                                    contentDescription = null,
                                    tint = SmartAirColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = "Room Diagnosis",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SmartAirColors.OnSurface
                        )
                    }
                    Text(
                        text = "Just now",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartAirColors.OnSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = state.aiInsight,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SmartAirColors.OnSurface,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sub-impact highlights
                if (state.currentReading.fan) {
                    ImpactHighlight(
                        icon = Icons.Outlined.Air,
                        title = "Ventilation impact",
                        subtitle = "Fan is active, improving circulation.",
                        iconTint = SmartAirColors.Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                ImpactHighlight(
                    icon = Icons.Outlined.Thermostat,
                    title = "Thermal comfort",
                    subtitle = state.currentReading.temperature?.let {
                        "%.1f°C — ${if (it > 30) "warm conditions" else "comfortable range"}"
                            .format(it)
                    } ?: "No temperature data",
                    iconTint = SmartAirColors.PrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ImpactHighlight(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = SmartAirColors.SurfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp),
                tint = iconTint
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SmartAirColors.OnSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = SmartAirColors.OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SuggestedPrompts(onPromptClick: (String) -> Unit) {
    Column {
        Text(
            text = "SUGGESTED EXPLORATION",
            style = MaterialTheme.typography.labelLarge,
            color = SmartAirColors.OnSurfaceVariant,
            letterSpacing = 0.08.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "Why did the status change?",
                "Is the room improving?",
                "Explain today's readings",
                "Should I keep the fan on?"
            ).forEach { prompt ->
                Surface(
                    onClick = { onPromptClick(prompt) },
                    shape = CircleShape,
                    color = SmartAirColors.SurfaceContainerHigh
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartAirColors.OnSurface,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserBubble(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 4.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = SmartAirColors.Primary,
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun AiBubble(message: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = SmartAirColors.SecondaryContainer,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SmartAirColors.OnSecondaryContainer
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = SmartAirColors.SurfaceContainerLowest,
            shadowElevation = 1.dp
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SmartAirColors.OnSurface,
                modifier = Modifier.padding(16.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ChatInput(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Mic button
            IconButton(
                onClick = { /* Voice input placeholder */ },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Mic,
                    contentDescription = "Voice input",
                    modifier = Modifier.size(20.dp),
                    tint = SmartAirColors.OnSurfaceVariant
                )
            }

            // Text input
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Ask about your room...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = SmartAirColors.OnSurface
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            // Send button
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(40.dp)
                    .background(SmartAirColors.Primary, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ArrowUpward,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
            }
        }
    }
}
