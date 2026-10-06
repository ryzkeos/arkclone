package com.arkeos.arkclone.ui.components

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.engine.RealAppIconView
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.model.CloneInstallState
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.DarkCardBorder
import com.arkeos.arkclone.ui.theme.DarkSurface
import com.arkeos.arkclone.ui.theme.DarkSurfaceVariant
import com.arkeos.arkclone.ui.theme.GrayMedium
import com.arkeos.arkclone.ui.theme.GraySubtle
import com.arkeos.arkclone.ui.theme.PureBlack
import com.arkeos.arkclone.ui.theme.PureWhite
import com.arkeos.arkclone.ui.theme.SilverAccent

@Composable
fun InstanceCard(
    instance: ClonedInstance,
    onLaunch: () -> Unit,
    onLaunchRealAppDirectly: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val isInstalled = instance.installState == CloneInstallState.INSTALLED

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(
                1.dp,
                if (isInstalled) PureWhite else DarkCardBorder,
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

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${instance.appName} #${instance.cloneNumber}",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = PureWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }

                        Text(
                            text = "${instance.alias} • ${instance.packageName}",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = GraySubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Status & Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isInstalled) PureWhite else GrayMedium)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = instance.installState.label.uppercase(),
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isInstalled) PureWhite else GrayMedium
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
                        text = "DETAILS",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = SilverAccent
                    )
                }
            }
        }
    }
}
