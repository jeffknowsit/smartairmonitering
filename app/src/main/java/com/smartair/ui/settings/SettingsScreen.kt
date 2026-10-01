package com.smartair.ui.settings

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartair.data.model.AppSettings
import com.smartair.data.model.ConnectionState
import com.smartair.ui.components.StitchToggle
import com.smartair.ui.theme.SmartAirColors

/**
 * Settings screen.
 * Faithfully reproduces the Stitch Settings design.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    connectionState: ConnectionState,
    onSettingsChange: (AppSettings) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        // Header Context
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "SmartAir System & Device Preferences",
            style = MaterialTheme.typography.bodyMedium,
            color = SmartAirColors.OnSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Arduino Telemetry Card
        ArduinoCard(
            connectionState = connectionState,
            onConnect = onConnect,
            onDisconnect = onDisconnect
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Room & Hardware
        SettingsSection(title = "ROOM & HARDWARE") {
            SettingsRow(
                icon = Icons.Outlined.MeetingRoom,
                title = "Room",
                trailing = {
                    SettingsValueChevron(value = settings.roomName)
                }
            )
            SettingsToggleRow(
                icon = Icons.Outlined.Usb,
                title = "Arduino Connection",
                subtitle = "USB OTG Auto-detect",
                checked = settings.arduinoAutoConnect,
                onCheckedChange = {
                    onSettingsChange(settings.copy(arduinoAutoConnect = it))
                }
            )
            SettingsRow(
                icon = Icons.Outlined.Air,
                title = "Fan Relay Control",
                trailing = {
                    SettingsValueChevron(value = "Auto (Threshold)")
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Alerts & Thresholds
        SettingsSection(title = "ALERTS & THRESHOLDS") {
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                title = "Notifications",
                trailing = {
                    SettingsValueChevron(value = settings.notificationPriority)
                }
            )
            SettingsToggleRow(
                icon = Icons.Outlined.Campaign,
                title = "Arduino Buzzer Alarm",
                subtitle = if (settings.buzzerEnabled) "Enabled for Critical" else "Disabled",
                checked = settings.buzzerEnabled,
                onCheckedChange = {
                    onSettingsChange(settings.copy(buzzerEnabled = it))
                }
            )
            SettingsRow(
                icon = Icons.Outlined.Grain,
                title = "Dust Warning Level",
                trailing = {
                    SettingsValueChevron(
                        value = settings.dustWarningThreshold.toString(),
                        isMono = true
                    )
                }
            )
            SettingsRow(
                icon = Icons.Outlined.Cloud,
                title = "MQ-5 Gas Warning",
                trailing = {
                    SettingsValueChevron(
                        value = settings.gasWarningThreshold.toString(),
                        isMono = true
                    )
                }
            )
            // SMS Settings
            SettingsToggleRow(
                icon = Icons.Outlined.Sms,
                title = "SMS Alerts",
                subtitle = if (settings.smsEnabled) "Enabled — Critical only" else "Disabled",
                checked = settings.smsEnabled,
                onCheckedChange = {
                    onSettingsChange(settings.copy(smsEnabled = it))
                }
            )
            
            if (settings.smsEnabled) {
                var phone by remember { mutableStateOf(settings.smsRecipient) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { 
                            phone = it
                            onSettingsChange(settings.copy(smsRecipient = it)) 
                        },
                        label = { Text("Emergency Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SmartAirColors.Primary,
                            unfocusedBorderColor = SmartAirColors.Outline
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: AI Assistant
        SettingsSection(title = "AI ASSISTANT") {
            SettingsToggleRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "AI Suggestions",
                checked = settings.aiSuggestionsEnabled,
                onCheckedChange = {
                    onSettingsChange(settings.copy(aiSuggestionsEnabled = it))
                }
            )
            SettingsRow(
                icon = Icons.Outlined.History,
                title = "Context Window",
                trailing = {
                    SettingsValueChevron(value = "Last ${settings.aiContextWindowHours} hours")
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Appearance & System
        SettingsSection(title = "APPEARANCE & SYSTEM") {
            SettingsRow(
                icon = Icons.Outlined.Palette,
                title = "Appearance",
                trailing = {
                    SettingsValueChevron(value = settings.appearance)
                }
            )
            SettingsToggleRow(
                icon = Icons.Outlined.Visibility,
                title = "Keep Screen Awake",
                subtitle = "While connected",
                checked = settings.keepScreenAwake,
                onCheckedChange = {
                    onSettingsChange(settings.copy(keepScreenAwake = it))
                }
            )
            // Mock mode developer toggle
            SettingsToggleRow(
                icon = Icons.Outlined.DeveloperMode,
                title = "Mock Mode (Dev)",
                subtitle = "Use simulated sensor data",
                checked = settings.isMockMode,
                onCheckedChange = {
                    onSettingsChange(settings.copy(isMockMode = it))
                }
            )
            SettingsRow(
                icon = Icons.Outlined.Info,
                title = "About SmartAir",
                trailing = {
                    Text(
                        text = "v1.0.0 (Uno)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace
                        ),
                        color = SmartAirColors.OnSurfaceVariant
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = SmartAirColors.SurfaceContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.Verified,
                        contentDescription = null,
                        tint = SmartAirColors.Primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "All hardware buses operational",
                style = MaterialTheme.typography.bodySmall,
                color = SmartAirColors.OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "SMARTAIR BIOPHILIC TELEMETRY · 2025",
                style = MaterialTheme.typography.labelLarge,
                color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ArduinoCard(
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = SmartAirColors.SurfaceContainerLowest,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SmartAirColors.Primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.DeveloperBoard,
                                contentDescription = null,
                                tint = SmartAirColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "Arduino Uno (USB OTG)",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = SmartAirColors.OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val (dotColor, statusText) = when (connectionState) {
                                ConnectionState.CONNECTED -> SmartAirColors.Primary to "Connected"
                                ConnectionState.CONNECTING -> SmartAirColors.Primary to "Connecting..."
                                ConnectionState.DISCONNECTED -> SmartAirColors.Outline to "Disconnected"
                                ConnectionState.PERMISSION_REQUIRED -> SmartAirColors.StatusWarning to "Permission Required"
                                ConnectionState.ERROR -> SmartAirColors.Error to "Error"
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = dotColor
                            )
                        }
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = SmartAirColors.SecondaryContainer.copy(alpha = 0.3f)
                ) {
                    Text(
                        text = "9600 BAUD",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SmartAirColors.OnSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Firmware & Sensors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Code,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmartAirColors.Primary
                    )
                    Text(
                        text = "Firmware",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartAirColors.OnSurfaceVariant
                    )
                }
                Text(
                    text = "SmartAir_v1.4.ino",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = SmartAirColors.OnSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Sensors,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SmartAirColors.Primary
                    )
                    Text(
                        text = "Sensors",
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartAirColors.OnSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DHT11", "Dust Sensor", "MQ-5 Gas").forEach { sensor ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SmartAirColors.SurfaceContainer
                        ) {
                            Text(
                                text = sensor,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = SmartAirColors.OnSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onDisconnect,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Disconnect")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = onConnect,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SmartAirColors.Primary)
                ) {
                    Text("Reconnect")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.08.sp
            ),
            color = SmartAirColors.OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SmartAirColors.SurfaceContainerLowest,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SmartAirColors.SurfaceContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SmartAirColors.OnSurfaceVariant
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = SmartAirColors.OnSurface
            )
        }
        trailing()
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SmartAirColors.SurfaceContainer,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = SmartAirColors.OnSurfaceVariant
                    )
                }
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = SmartAirColors.OnSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = SmartAirColors.OnSurfaceVariant
                    )
                }
            }
        }
        StitchToggle(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsValueChevron(value: String, isMono: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default
            ),
            color = SmartAirColors.OnSurfaceVariant
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
