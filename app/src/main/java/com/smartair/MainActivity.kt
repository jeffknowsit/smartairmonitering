package com.smartair

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartair.data.model.ConnectionState
import com.smartair.ui.SmartAirViewModel
import com.smartair.ui.ai.AiScreen
import com.smartair.ui.components.ConnectionBadge
import com.smartair.ui.history.HistoryScreen
import com.smartair.ui.home.HomeScreen
import com.smartair.ui.settings.SettingsScreen
import com.smartair.ui.theme.SmartAirColors
import com.smartair.ui.theme.SmartAirTheme
import java.text.SimpleDateFormat
import java.util.*

import androidx.activity.viewModels
import android.content.Intent

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* handled */ }

    private val viewModel: SmartAirViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Request notification and SMS permission
        val permissions = mutableListOf(Manifest.permission.SEND_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        
        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }

        setContent {
            SmartAirTheme {
                val settings by viewModel.settings.collectAsState()

                // Keep screen awake if enabled
                LaunchedEffect(settings.keepScreenAwake) {
                    if (settings.keepScreenAwake) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }
                
                // Register USB receivers and auto-connect
                DisposableEffect(Unit) {
                    viewModel.registerUsbReceivers()
                    // If auto-connect is enabled in settings or it's just app startup, attempt connection
                    if (settings.arduinoAutoConnect) {
                        viewModel.connectArduino()
                    }
                    onDispose {
                        viewModel.unregisterUsbReceivers()
                    }
                }

                SmartAirNavigation(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == android.hardware.usb.UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            viewModel.connectArduino()
        }
    }

    override fun onResume() {
        super.onResume()
        handleIntent(intent)
    }

    override fun onPause() {
        super.onPause()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartAirNavigation(viewModel: SmartAirViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val dashboardState by viewModel.dashboardState.collectAsState()
    val historyState by viewModel.historyState.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val mockScenario by viewModel.selectedMockScenario.collectAsState()
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    val showBottomNav = currentRoute in listOf("home", "history", "ai")

    Scaffold(
        containerColor = SmartAirColors.Surface,
        topBar = {
            if (currentRoute != "settings") {
                SmartAirTopBar(
                    connectionState = dashboardState.connectionState,
                    roomName = settings.roomName,
                    onSettingsClick = { navController.navigate("settings") }
                )
            } else {
                SettingsTopBar(onBack = { navController.popBackStack() })
            }
        },
        bottomBar = {
            if (showBottomNav) {
                SmartAirBottomNav(
                    currentRoute = currentRoute ?: "home",
                    onNavigate = { route ->
                        if (route != currentRoute) {
                            navController.navigate(route) {
                                popUpTo("home") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(
                    state = dashboardState,
                    mockScenario = mockScenario,
                    onMockScenarioChange = { viewModel.setMockScenario(it) },
                    onAskAi = {
                        navController.navigate("ai") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onRefreshInsight = { viewModel.refreshAiInsight() }
                )
            }
            composable("history") {
                LaunchedEffect(Unit) {
                    viewModel.loadHistory()
                }
                HistoryScreen(
                    state = historyState,
                    onRangeSelected = { viewModel.loadHistory(it) },
                    onMetricSelected = { viewModel.selectMetric(it) }
                )
            }
            composable("ai") {
                AiScreen(
                    dashboardState = dashboardState,
                    chatMessages = chatMessages,
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onClearChat = { viewModel.clearChat() }
                )
            }
            composable("settings") {
                SettingsScreen(
                    settings = settings,
                    connectionState = dashboardState.connectionState,
                    onSettingsChange = { viewModel.updateSettings(it) },
                    onConnect = { viewModel.connectArduino() },
                    onDisconnect = { viewModel.disconnectArduino() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SmartAirTopBar(
    connectionState: ConnectionState,
    roomName: String,
    onSettingsClick: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    var currentTime by remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = timeFormat.format(Date())
            kotlinx.coroutines.delay(30_000)
        }
    }

    Surface(
        color = SmartAirColors.Surface.copy(alpha = 0.85f),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: App name + connection badge
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "SmartAir",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SmartAirColors.OnSurface
                    )
                    ConnectionBadge(state = connectionState)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = roomName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = SmartAirColors.OnSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = currentTime,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = SmartAirColors.OnSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Right: Settings + Avatar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(22.dp),
                        tint = SmartAirColors.OnSurfaceVariant
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = SmartAirColors.Primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onBack: () -> Unit) {
    Surface(
        color = SmartAirColors.Surface.copy(alpha = 0.85f),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .offset(x = (-8).dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        modifier = Modifier.size(24.dp),
                        tint = SmartAirColors.OnSurfaceVariant
                    )
                }
                Text(
                    text = "Device Settings",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SmartAirColors.OnSurface
                )
            }
            Surface(
                shape = CircleShape,
                color = SmartAirColors.Primary,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartAirBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        Triple("home", "Home", Icons.Outlined.Dashboard),
        Triple("history", "History", Icons.Outlined.ShowChart),
        Triple("ai", "AI", Icons.Outlined.AutoAwesome)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = SmartAirColors.SurfaceContainerLowest.copy(alpha = 0.9f),
            shadowElevation = 8.dp,
            modifier = Modifier.widthIn(max = 360.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (route, label, icon) ->
                    val isSelected = route == currentRoute
                    val color = if (isSelected) SmartAirColors.Primary
                                else SmartAirColors.OnSurfaceVariant

                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onNavigate(route) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            modifier = Modifier.size(24.dp),
                            tint = color
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                        // Active indicator dot
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SmartAirColors.Primary)
                            )
                        }
                    }
                }
            }
        }
    }
}
