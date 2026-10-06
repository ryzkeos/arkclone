package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.engine.ArkCloneViewModel
import com.example.engine.ArkNotificationEngine
import com.example.ui.components.StartupSplash
import com.example.ui.screens.CloneAppDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InstanceSwitcherSheet
import com.example.ui.screens.NotificationCenterDialog
import com.example.ui.screens.ResourceLimiterDialog
import com.example.ui.screens.VirtualAppRunnerScreen
import com.example.ui.theme.ArkCloneTheme
import com.example.ui.theme.PureBlack

class MainActivity : ComponentActivity() {

    private val viewModel: ArkCloneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIntent(intent)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            ArkCloneTheme(darkTheme = isDarkMode) {
                MainAppContent(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode,
                    onCheckPermissions = { requestNotificationPermission() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val targetInstanceId = intent?.getStringExtra(ArkNotificationEngine.EXTRA_TARGET_INSTANCE_ID)
        if (!targetInstanceId.isNullOrEmpty()) {
            viewModel.launchInstance(targetInstanceId, launchRealApp = true)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: ArkCloneViewModel,
    isDarkMode: Boolean,
    onCheckPermissions: () -> Unit
) {
    var isSplashActive by remember { mutableStateOf(true) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val instances by viewModel.instances.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val activeInstanceId by viewModel.activeInstanceId.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isCleaningCache by viewModel.isCleaningCache.collectAsState()
    val cleanResult by viewModel.cleanResult.collectAsState()
    val isEcoActive by viewModel.isEcoSaverActive.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val showCloneDialog by viewModel.showCloneDialog.collectAsState()
    val showSwitcherSheet by viewModel.showSwitcherSheet.collectAsState()
    val showResourceLimiter by viewModel.showResourceLimiter.collectAsState()
    val showNotificationCenter by viewModel.showNotificationCenter.collectAsState()

    val totalMemoryMb = instances.map { it.memoryUsageMb }.sum()
    val totalCacheBytes = instances.map { it.cacheSizeBytes }.sum()
    val totalCacheFormatted = viewModel.formatBytes(totalCacheBytes)
    val unreadNotificationsCount = notifications.count { !it.isRead }

    val currentInstance = instances.find { it.id == activeInstanceId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        if (currentInstance != null) {
            // Virtual Space Enclave HUD & Runner for Real APK
            VirtualAppRunnerScreen(
                currentInstance = currentInstance,
                allInstances = instances,
                onExit = { viewModel.exitSandbox() },
                onLaunchRealAppDirectly = {
                    viewModel.startRealApkProcess(currentInstance)
                },
                onOpenAppSettings = {
                    viewModel.openRealAppSettings(currentInstance.packageName)
                },
                onSwitchInstance = { targetId -> viewModel.switchInstance(targetId) },
                onToggleSleep = { id -> viewModel.toggleInstanceSleep(id) },
                onClearCache = { id -> viewModel.clearSingleInstanceCache(id) },
                onSimulatePush = { id, title, body ->
                    viewModel.simulateNotificationForInstance(id, title, body)
                }
            )
        } else {
            // Clean Dashboard Layout (Switch button removed from main page per user request)
            DashboardScreen(
                instances = instances,
                installedApps = installedApps,
                unreadCount = unreadNotificationsCount,
                isDarkMode = isDarkMode,
                isEcoActive = isEcoActive,
                isCleaningCache = isCleaningCache,
                cleanResult = cleanResult,
                selectedCategory = selectedCategory,
                totalCacheFormatted = totalCacheFormatted,
                totalMemoryMb = totalMemoryMb,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onCategorySelect = { cat -> viewModel.setCategoryFilter(cat) },
                onOpenCloneDialog = {
                    viewModel.scanInstalledApks()
                    viewModel.setShowCloneDialog(true)
                },
                onOpenNotificationCenter = { viewModel.setShowNotificationCenter(true) },
                onOpenResourceLimiter = { viewModel.setShowResourceLimiter(true) },
                onOneTapCleanCache = { viewModel.oneTapCleanAllCache() },
                onDismissCleanResult = { viewModel.dismissCleanResult() },
                onQuickClone = { app -> viewModel.quickCloneSuggestedApk(app) },
                onLaunchInstance = { id -> viewModel.launchInstance(id, launchRealApp = false) },
                onLaunchRealAppDirectly = { id -> viewModel.launchInstance(id, launchRealApp = true) },
                onOpenAppSettings = { pkg -> viewModel.openRealAppSettings(pkg) },
                onToggleSleep = { id -> viewModel.toggleInstanceSleep(id) },
                onClearInstanceCache = { id -> viewModel.clearSingleInstanceCache(id) },
                onSimulateNotification = { id ->
                    val inst = instances.find { it.id == id }
                    viewModel.simulateNotificationForInstance(
                        id,
                        "Important Update",
                        "Account synchronization active for ${inst?.appName ?: "APK"}."
                    )
                },
                onDeleteInstance = { id -> viewModel.deleteInstance(id) }
            )
        }

        // Modals & Bottom Sheets
        if (showCloneDialog) {
            CloneAppDialog(
                availableApps = installedApps,
                isScanning = isScanning,
                onRescan = { viewModel.scanInstalledApks() },
                onDismiss = { viewModel.setShowCloneDialog(false) },
                onConfirmClone = { targetApk, count, alias, category, priority ->
                    viewModel.cloneRealApk(targetApk, count, alias, category, priority)
                }
            )
        }

        if (showSwitcherSheet) {
            InstanceSwitcherSheet(
                instances = instances,
                activeInstanceId = activeInstanceId,
                onDismiss = { viewModel.setShowSwitcherSheet(false) },
                onSelectInstance = { id -> viewModel.switchInstance(id) }
            )
        }

        if (showNotificationCenter) {
            NotificationCenterDialog(
                notifications = notifications,
                onDismiss = { viewModel.setShowNotificationCenter(false) },
                onSelectNotification = { instanceId ->
                    viewModel.setShowNotificationCenter(false)
                    viewModel.launchInstance(instanceId, launchRealApp = true)
                },
                onClearAll = { viewModel.clearAllNotifications() }
            )
        }

        if (showResourceLimiter) {
            ResourceLimiterDialog(
                isEcoActive = isEcoActive,
                onToggleEco = { viewModel.toggleEcoSaver() },
                onDismiss = { viewModel.setShowResourceLimiter(false) },
                onTrimMemory = {
                    System.gc()
                }
            )
        }

        // Startup splash overlay
        if (isSplashActive) {
            StartupSplash(
                onSplashFinished = { isSplashActive = false }
            )
        }
    }
}
