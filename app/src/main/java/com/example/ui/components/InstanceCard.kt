package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun InstanceCard(
    instance: ClonedInstance,
    onLaunch: () -> Unit,
    onLaunchRealAppDirectly: () -> Unit,
    onToggleSleep: () -> Unit,
    onClearCache: () -> Unit,
    onSimulateNotification: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isRunning = instance.status == InstanceStatus.RUNNING

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(
                1.dp,
                if (isRunning) PureWhite else DarkCardBorder,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
            .testTag("instance_card_${instance.id}")
    ) {
        Column {
            // Header Row: Real App Icon, App Name, Badge, Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Real App Icon extracted directly from device package
                    RealAppIconView(
                        packageName = instance.packageName,
                        appName = instance.appName,
                        size = 40.dp
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${instance.appName} #${instance.cloneNumber}",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Badge Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PureBlack)
                                    .border(1.dp, DarkCardBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = instance.badgeTag,
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 0.5.sp,
                                    color = SilverAccent
                                )
                            }
                        }

                        Text(
                            text = "${instance.alias} • ${instance.packageName}",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = GraySubtle
                        )
                    }
                }

                // Status & Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) PureWhite else GrayMedium)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = if (isRunning) "ACTIVE" else "SLEEP",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isRunning) PureWhite else GrayMedium
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = GraySubtle,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier
                                .background(DarkSurface)
                                .border(1.dp, DarkCardBorder)
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Launch Application",
                                        fontFamily = ChakraPetchFontFamily,
                                        color = PureWhite,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onLaunchRealAppDirectly()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Send Push Alert to Account",
                                        fontFamily = ChakraPetchFontFamily,
                                        color = PureWhite,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Send,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onSimulateNotification()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Clear Cache (${instance.cacheSizeBytes / (1024 * 1024)} MB)",
                                        fontFamily = ChakraPetchFontFamily,
                                        color = PureWhite,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.CleaningServices,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onClearCache()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "App Info (Settings)",
                                        fontFamily = ChakraPetchFontFamily,
                                        color = PureWhite,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onOpenAppSettings()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        if (isRunning) "Hibernate Instance" else "Wake Instance",
                                        fontFamily = ChakraPetchFontFamily,
                                        color = PureWhite,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.PowerSettingsNew,
                                        contentDescription = null,
                                        tint = PureWhite,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onToggleSleep()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Delete Virtual Enclave",
                                        fontFamily = ChakraPetchFontFamily,
                                        color = PureWhite,
                                        fontSize = 12.sp
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = GrayMedium,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Telemetry Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(PureBlack)
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "VERSION: ",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = GrayMedium
                    )
                    Text(
                        text = "v${instance.realVersionName} (${instance.realApkSizeBytes / (1024 * 1024)} MB)",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = PureWhite
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "CACHE: ",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = GrayMedium
                    )
                    Text(
                        text = "${instance.cacheSizeBytes / (1024 * 1024)} MB",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = SilverAccent
                    )
                }

                if (instance.pendingNotifications > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notifications",
                            tint = PureWhite,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${instance.pendingNotifications} PUSH",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = PureWhite
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Launch Cloned App, Enclave Console
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary: Launch App in Virtual Space
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PureWhite)
                        .clickable { onLaunchRealAppDirectly() }
                        .padding(vertical = 10.dp)
                        .testTag("launch_button_${instance.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Launch",
                            tint = PureBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LAUNCH",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = PureBlack
                        )
                    }
                }

                // Enclave Console Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onLaunch() }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("enclave_console_button_${instance.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V-SPACE HUD",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = SilverAccent
                    )
                }

                // Hibernate / Sleep Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleSleep() }
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                        .testTag("sleep_button_${instance.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Hibernate",
                        tint = if (isRunning) SilverAccent else GrayMedium,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
