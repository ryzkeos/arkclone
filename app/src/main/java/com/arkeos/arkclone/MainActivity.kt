package com.arkeos.arkclone

import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.arkeos.arkclone.engine.ArkCloneViewModel
import com.arkeos.arkclone.model.CloneInstallState
import com.arkeos.arkclone.ui.components.StartupSplash
import com.arkeos.arkclone.ui.screens.CloneAppDialog
import com.arkeos.arkclone.ui.screens.CloneDetailsScreen
import com.arkeos.arkclone.ui.screens.DashboardScreen
import com.arkeos.arkclone.ui.theme.ArkCloneTheme
import com.arkeos.arkclone.ui.theme.PureBlack
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: ArkCloneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            ArkCloneTheme(darkTheme = isDarkMode) {
                MainAppContent(
                    viewModel = viewModel,
                    isDarkMode = isDarkMode
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshInstallStates()
    }
}

@Composable
fun MainAppContent(
    viewModel: ArkCloneViewModel,
    isDarkMode: Boolean
) {
    var isSplashActive by remember { mutableStateOf(true) }

    val instances by viewModel.instances.collectAsState()
    val activeInstanceId by viewModel.activeInstanceId.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val showCloneDialog by viewModel.showCloneDialog.collectAsState()

    val context = LocalContext.current
    val freeRamBytes by produceState(initialValue = 0L) {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        while (true) {
            val info = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(info)
            value = info.availMem
            delay(3000)
        }
    }
    val installedCount = instances.count { it.installState == CloneInstallState.INSTALLED }
    val apkSizeFormatted = viewModel.formatBytes(
        instances.filter { it.installState == CloneInstallState.INSTALLED }.sumOf { it.realApkSizeBytes }
    )
    val freeRamFormatted = viewModel.formatBytes(freeRamBytes)

    val currentInstance = instances.find { it.id == activeInstanceId }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        if (currentInstance != null) {
            CloneDetailsScreen(
                instance = currentInstance,
                onExit = { viewModel.closeDetails() },
                onLaunch = { viewModel.startRealApkProcess(currentInstance) },
                onOpenAppSettings = {
                    viewModel.openRealAppSettings(currentInstance.clonePackageName.ifBlank { currentInstance.packageName })
                }
            )
        } else {
            DashboardScreen(
                instances = instances,
                installedApps = installedApps,
                isDarkMode = isDarkMode,
                selectedCategory = selectedCategory,
                installedCount = installedCount,
                freeRamFormatted = freeRamFormatted,
                apkSizeFormatted = apkSizeFormatted,
                onToggleDarkMode = { viewModel.toggleDarkMode() },
                onCategorySelect = { cat -> viewModel.setCategoryFilter(cat) },
                onOpenCloneDialog = {
                    viewModel.scanInstalledApks()
                    viewModel.setShowCloneDialog(true)
                },
                onQuickClone = { app -> viewModel.quickCloneSuggestedApk(app) },
                onLaunchInstance = { id -> viewModel.openDetails(id) },
                onLaunchRealAppDirectly = { id -> viewModel.launchInstance(id) },
                onOpenAppSettings = { pkg -> viewModel.openRealAppSettings(pkg) },
                onDeleteInstance = { id -> viewModel.deleteInstance(id) }
            )
        }

        // Modals & Bottom Sheets
        if (showCloneDialog) {
            CloneAppDialog(
                availableApps = installedApps,
                isScanning = isScanning,
                onDismiss = { viewModel.setShowCloneDialog(false) },
                onConfirmClone = { targetApk, count, aliases, category ->
                    viewModel.cloneRealApk(targetApk, count, aliases, category)
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
