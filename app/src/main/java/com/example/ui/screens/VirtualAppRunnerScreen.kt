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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextOverflow
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
    onSimulatePush: (String, String, String) -> Unit,
    onCompleteSetup: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onResetToFreshInstall: (String) -> Unit = {},
    onUpdateNotes: (String, String) -> Unit = { _, _ -> }
) {
    BackHandler { onExit() }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Account setup inputs
    var setupUsername by remember(currentInstance.id) {
        mutableStateOf(currentInstance.userSession.username.ifBlank { "${currentInstance.appName} #${currentInstance.cloneNumber}" })
    }
    var setupEmailOrPhone by remember(currentInstance.id) {
        mutableStateOf(currentInstance.userSession.registeredPhoneOrEmail)
    }
    var setupNotes by remember(currentInstance.id) {
        mutableStateOf(currentInstance.userSession.isolatedNotes)
    }
    var notesSaveFeedback by remember { mutableStateOf<String?>(null) }

    // Push inputs
    var notificationTitleInput by remember(currentInstance.id) {
        mutableStateOf("Alert: ${currentInstance.alias}")
    }
    var notificationBodyInput by remember(currentInstance.id) {
        mutableStateOf("Background task sync completed in isolated enclave.")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        // Top Container Header: Ark Clone Virtual Space Bar with statusBarsPadding to avoid notification area overlap
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .statusBarsPadding()
                .border(1.dp, DarkCardBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
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

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${currentInstance.appName} #${currentInstance.cloneNumber}",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PureWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
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
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                            color = GraySubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Header Right Side: Switch button removed per user request; icon only actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(
                    onClick = { onToggleSleep(currentInstance.id) },
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("header_toggle_sleep_button")
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

        // Sub Navigation Tabs
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
                // Tab 0: Separated Clean App Instance (Freshly installed & zero host overlap)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Separation & Zero Overlap Guarantee Banner
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SEPARATED APP INSTANCE",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp,
                                            color = PureWhite
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (currentInstance.userSession.isAccountSetupComplete) PureWhite else SilverAccent)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (currentInstance.userSession.isAccountSetupComplete) "SEPARATED PROFILE" else "FRESH INSTALL",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            letterSpacing = 0.5.sp,
                                            color = PureBlack
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Each clone operates as a completely separated, freshly installed application. Host device app data remains 100% untouched and inaccessible. Clones are never connected to each other.",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = GraySubtle
                                )
                            }
                        }
                    }

                    // Fresh Setup Wizard or Active Separated Profile
                    if (!currentInstance.userSession.isAccountSetupComplete) {
                        // Fresh App Onboarding Wizard
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, PureWhite, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = PureWhite,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "INITIAL SETUP FOR CLONE #${currentInstance.cloneNumber}",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                letterSpacing = 0.5.sp,
                                                color = PureWhite
                                            )
                                            Text(
                                                text = "Configure this fresh instance without touching real device accounts:",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 10.sp,
                                                color = GraySubtle
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = setupUsername,
                                        onValueChange = { setupUsername = it },
                                        label = {
                                            Text(
                                                "Clone Account Name / Label",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 11.sp
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PureWhite,
                                            unfocusedBorderColor = DarkCardBorder,
                                            focusedTextColor = PureWhite,
                                            unfocusedTextColor = PureWhite,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = setupEmailOrPhone,
                                        onValueChange = { setupEmailOrPhone = it },
                                        label = {
                                            Text(
                                                "Separate Login Phone or Email (Optional)",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 11.sp
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                "e.g. clone${currentInstance.cloneNumber}@example.com",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 11.sp,
                                                color = GrayMedium
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PureWhite,
                                            unfocusedBorderColor = DarkCardBorder,
                                            focusedTextColor = PureWhite,
                                            unfocusedTextColor = PureWhite,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = setupNotes,
                                        onValueChange = { setupNotes = it },
                                        label = {
                                            Text(
                                                "Isolated Notes / Credentials Vault",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 11.sp
                                            )
                                        },
                                        placeholder = {
                                            Text(
                                                "Add private notes stored only in this clone...",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 11.sp,
                                                color = GrayMedium
                                            )
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = PureWhite,
                                            unfocusedBorderColor = DarkCardBorder,
                                            focusedTextColor = PureWhite,
                                            unfocusedTextColor = PureWhite,
                                            focusedContainerColor = DarkSurfaceVariant,
                                            unfocusedContainerColor = DarkSurfaceVariant
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PureWhite)
                                            .clickable {
                                                onCompleteSetup(
                                                    currentInstance.id,
                                                    setupUsername,
                                                    setupEmailOrPhone,
                                                    setupNotes
                                                )
                                            }
                                            .padding(vertical = 12.dp)
                                            .testTag("initialize_clone_account_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "INITIALIZE FRESH CLONE WORKSPACE",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            letterSpacing = 0.5.sp,
                                            color = PureBlack
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Active Separated Workspace Profile & Private Vault
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(DarkSurfaceVariant)
                                    .border(1.dp, PureWhite, RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(DarkSurface)
                                                    .border(1.dp, DarkCardBorder, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = currentInstance.userSession.username,
                                                    fontFamily = ChakraPetchFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = PureWhite,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${currentInstance.userSession.handle} • UID_${currentInstance.virtualUid}",
                                                    fontFamily = ChakraPetchFontFamily,
                                                    fontSize = 10.sp,
                                                    color = SilverAccent,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }

                                        // Factory Reset Clone button
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DarkSurface)
                                                .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
                                                .clickable { onResetToFreshInstall(currentInstance.id) }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                                .testTag("factory_reset_clone_button")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = "Reset",
                                                    tint = PureWhite,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "RESET TO CLEAN",
                                                    fontFamily = ChakraPetchFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = PureWhite
                                                )
                                            }
                                        }
                                    }

                                    if (currentInstance.userSession.registeredPhoneOrEmail.isNotBlank()) {
                                        Text(
                                            text = "Linked ID: ${currentInstance.userSession.registeredPhoneOrEmail}",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontSize = 10.sp,
                                            color = GraySubtle,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    // Isolated Notes Vault Editor
                                    Column {
                                        Text(
                                            text = "ISOLATED INSTANCE VAULT NOTES:",
                                            fontFamily = ChakraPetchFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp,
                                            color = GrayMedium
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        OutlinedTextField(
                                            value = setupNotes,
                                            onValueChange = {
                                                setupNotes = it
                                                notesSaveFeedback = null
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

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = notesSaveFeedback ?: "Private to this clone directory",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontSize = 9.sp,
                                                color = if (notesSaveFeedback != null) PureWhite else GraySubtle
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(PureWhite)
                                                    .clickable {
                                                        onUpdateNotes(currentInstance.id, setupNotes)
                                                        notesSaveFeedback = "Saved to clone vault"
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                                    .testTag("save_clone_notes_button")
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Save,
                                                        contentDescription = null,
                                                        tint = PureBlack,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "SAVE NOTES",
                                                        fontFamily = ChakraPetchFontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp,
                                                        color = PureBlack
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Primary Launch Button
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(PureWhite)
                                .clickable { onLaunchRealAppDirectly() }
                                .padding(16.dp)
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
                                        text = "LAUNCH ISOLATED TASK",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        letterSpacing = 1.sp,
                                        color = PureBlack,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Spawns an independent multi-task instance for ${currentInstance.appName}",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        color = PureBlack.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Secondary App Actions: App Settings & Flush Cache
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
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Specifications Card with Safe Ellipsis Truncation
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
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
                                        color = PureWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
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
                                        color = SilverAccent,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
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
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
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
                                            color = GraySubtle,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Account Push Notification Routing
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
                                color = GraySubtle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Storage & Cache Inspector
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
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
                                color = SilverAccent,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
