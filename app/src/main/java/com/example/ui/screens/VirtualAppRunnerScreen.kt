package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RealAppIconView
import com.example.model.ClonedInstance
import com.example.model.InstanceStatus
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GrayMedium
import com.example.ui.theme.GraySubtle
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverAccent

@Composable
fun VirtualAppRunnerScreen(
    currentInstance: ClonedInstance,
    allInstances: List<ClonedInstance>,
    onExit: () -> Unit,
    onLaunchRealAppDirectly: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onSwitchInstance: (String) -> Unit,
    onToggleSleep: (String) -> Unit,
    onClearCache: (String) -> Unit,
    onSimulatePush: (String, String, String) -> Unit
) {
    BackHandler { onExit() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSwitchDropdown by remember { mutableStateOf(false) }

    var notificationTitleInput by remember { mutableStateOf("Alert: ${currentInstance.alias}") }
    var notificationBodyInput by remember { mutableStateOf("Background task sync completed in isolated enclave.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Top Container Header: Ark Clone Virtual Space Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .border(1.dp, DarkCardBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("exit_sandbox_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Exit to Dashboard",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                RealAppIconView(
                    packageName = currentInstance.packageName,
                    appName = currentInstance.appName,
                    size = 32.dp,
                    shapeRadius = 6.dp
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${currentInstance.appName} #${currentInstance.cloneNumber}",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PureWhite
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PureBlack)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentInstance.badgeTag,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = PureWhite
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Isolated",
                            tint = PureWhite,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "VIRTUAL ENCLAVE • ${currentInstance.packageName}",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp,
                            letterSpacing = 0.5.sp,
                            color = GraySubtle
                        )
                    }
                }
            }

            // Quick Instance Switcher
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, PureWhite, RoundedCornerShape(8.dp))
                            .clickable { showSwitchDropdown = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("quick_switch_dropdown_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Switch Instance",
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SWITCH",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = PureWhite
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showSwitchDropdown,
                        onDismissRequest = { showSwitchDropdown = false },
                        modifier = Modifier
                            .background(DarkSurface)
                            .border(1.dp, DarkCardBorder)
                    ) {
                        Text(
                            text = "SMOOTH SWITCH INSTANCE",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = GrayMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )

                        allInstances.forEach { other ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "${other.appName} #${other.cloneNumber} [${other.badgeTag}]",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = if (other.id == currentInstance.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (other.id == currentInstance.id) PureWhite else GraySubtle,
                                            fontSize = 12.sp
                                        )
                                        if (other.id == currentInstance.id) {
                                            Text(
                                                text = "CURRENT",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = PureWhite
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    showSwitchDropdown = false
                                    if (other.id != currentInstance.id) {
                                        onSwitchInstance(other.id)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = { onToggleSleep(currentInstance.id) },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Hibernate",
                        tint = if (currentInstance.status == InstanceStatus.RUNNING) PureWhite else GrayMedium,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Sub Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = PureWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PureWhite,
                    height = 2.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "VIRTUAL RUNTIME",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "ACCOUNT PUSH",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "STORAGE & CACHE",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Real APK Runtime Console & Launch Enclave
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Big Action: Launch Real APK
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite)
                                .clickable { onLaunchRealAppDirectly() }
                                .padding(18.dp)
                                .testTag("primary_launch_real_apk_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Launch",
                                    tint = PureBlack,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "LAUNCH APPLICATION",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp,
                                        color = PureBlack
                                    )
                                    Text(
                                        text = "Executes ${currentInstance.appName} in this isolated virtual space",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        color = PureBlack.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Secondary App Actions
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onOpenAppSettings() }
                                    .padding(12.dp)
                                    .testTag("open_system_app_settings_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "APP SETTINGS",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                    .clickable { onClearCache(currentInstance.id) }
                                    .padding(12.dp)
                                    .testTag("clear_enclave_cache_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CleaningServices,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "FLUSH CACHE",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                }
                            }
                        }
                    }

                    // Real APK Metadata Enclave Box
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "APPLICATION SPECIFICATIONS",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp,
                                    color = PureWhite
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Package Name:",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontSize = 10.sp,
                                        color = GrayMedium
                                    )
                                    Text(
                                        text = currentInstance.packageName,
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = PureWhite
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "APK Version:",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontSize = 10.sp,
                                        color = GrayMedium
                                    )
                                    Text(
                                        text = "v${currentInstance.realVersionName} (Code ${currentInstance.realVersionCode})",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = SilverAccent
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Base APK Size:",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontSize = 10.sp,
                                        color = GrayMedium
                                    )
                                    Text(
                                        text = "${currentInstance.realApkSizeBytes / (1024 * 1024)} MB",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = PureWhite
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Virtual UID Assigned:",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontSize = 10.sp,
                                        color = GrayMedium
                                    )
                                    Text(
                                        text = "UID_${currentInstance.virtualUid}",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = PureWhite
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Target SDK Platform:",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontSize = 10.sp,
                                        color = GrayMedium
                                    )
                                    Text(
                                        text = "Android API ${currentInstance.targetSdkVersion}",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = SilverAccent
                                    )
                                }

                                if (currentInstance.realSourceApkPath.isNotBlank()) {
                                    Column {
                                        Text(
                                            text = "Host APK Binary Path:",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontSize = 10.sp,
                                            color = GrayMedium
                                        )
                                        Text(
                                            text = currentInstance.realSourceApkPath,
                                            fontFamily = ChakraPetchFontFamily,
                                            fontSize = 9.sp,
                                            color = GraySubtle
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Enclave isolation status
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "MULTI-TASK RUNTIME ISOLATION ACTIVE",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = PureWhite
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Each cloned instance executes with FLAG_ACTIVITY_MULTIPLE_TASK and independent TaskAffinity, preventing cross-account state collision on device.",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = GraySubtle
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Account Push Notification Routing
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ISOLATED PUSH NOTIFICATION BRIDGE",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Ark Clone creates dedicated system notification channels for this exact cloned instance (${currentInstance.appName} #${currentInstance.cloneNumber} [${currentInstance.badgeTag}]).",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = GraySubtle
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = notificationTitleInput,
                        onValueChange = { notificationTitleInput = it },
                        label = {
                            Text(
                                "Push Notification Title",
                                fontFamily = ChakraPetchFontFamily,
                                fontSize = 11.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PureWhite,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = notificationBodyInput,
                        onValueChange = { notificationBodyInput = it },
                        label = {
                            Text(
                                "Push Notification Payload / Message",
                                fontFamily = ChakraPetchFontFamily,
                                fontSize = 11.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PureWhite,
                            unfocusedBorderColor = DarkCardBorder,
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Trigger Push Notification Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(PureWhite)
                            .clickable {
                                onSimulatePush(
                                    currentInstance.id,
                                    notificationTitleInput,
                                    notificationBodyInput
                                )
                            }
                            .padding(vertical = 12.dp)
                            .testTag("simulate_push_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Push",
                                tint = PureBlack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DISPATCH PUSH TO THIS CLONE",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                letterSpacing = 1.sp,
                                color = PureBlack
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status pill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CHANNEL STATUS: ACTIVE & MAPPED",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = PureWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Incoming push packets will be automatically routed to enclaves at path: ${currentInstance.isolatedStoragePath}/notifs",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 10.sp,
                                color = GraySubtle
                            )
                        }
                    }
                }
            }

            2 -> {
                // Storage, Virtual Filesystem & Cache Inspector
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "VIRTUAL STORAGE ENCLAVE",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.sp,
                        color = PureWhite
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "This cloned instance has an isolated filesystem completely segregated from other clones and host system.",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = GraySubtle
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "ISOLATED DATA",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = GrayMedium
                                )
                                Text(
                                    text = "${currentInstance.dataSizeBytes / (1024 * 1024)} MB",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = "Databases & Key Vault",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 9.sp,
                                    color = GraySubtle
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "TEMP CACHE",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 10.sp,
                                    color = GrayMedium
                                )
                                Text(
                                    text = "${currentInstance.cacheSizeBytes / (1024 * 1024)} MB",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SilverAccent
                                )
                                Text(
                                    text = "Flushable Junk",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 9.sp,
                                    color = GraySubtle
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurface)
                            .border(1.dp, PureWhite, RoundedCornerShape(8.dp))
                            .clickable { onClearCache(currentInstance.id) }
                            .padding(vertical = 12.dp)
                            .testTag("clear_instance_cache_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = "Clear Cache",
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PURGE THIS INSTANCE CACHE (${currentInstance.cacheSizeBytes / (1024 * 1024)} MB)",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = PureWhite
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ISOLATED STORAGE MOUNT DIRECTORY",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = PureWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentInstance.isolatedStoragePath,
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = SilverAccent
                            )
                        }
                    }
                }
            }
        }
    }
}
