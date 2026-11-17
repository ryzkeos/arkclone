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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
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
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkSurface
import com.arkeos.arkclone.ui.theme.ArkSurfaceVariant
import com.arkeos.arkclone.ui.theme.ArkMuted
import com.arkeos.arkclone.ui.theme.ArkSubtle
import com.arkeos.arkclone.ui.theme.ArkPaper
import com.arkeos.arkclone.ui.theme.ArkAction
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkSoft

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
            .background(ArkSurfaceVariant)
            .border(
                1.dp,
                if (isInstalled) ArkInk else ArkBorder,
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
                        Text(
                            text = instance.alias,
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ArkInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = instance.clonePackageName.ifBlank { instance.packageName },
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = ArkSubtle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (instance.installState) {
                            CloneInstallState.INSTALLED -> Icons.Default.CheckCircle
                            CloneInstallState.PREPARING,
                            CloneInstallState.AWAITING_USER -> Icons.Default.HourglassEmpty
                            else -> Icons.Default.ErrorOutline
                        },
                        contentDescription = instance.installState.label,
                        tint = if (isInstalled) ArkInk else ArkMuted,
                        modifier = Modifier.size(14.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = instance.installState.label.uppercase(),
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp,
                        color = if (isInstalled) ArkInk else ArkMuted
                    )

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = ArkSubtle,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        InstanceActionMenu(
                            expanded = showMenu,
                            onDismiss = { showMenu = false },
                            onLaunchRealAppDirectly = onLaunchRealAppDirectly,
                            onOpenAppSettings = onOpenAppSettings,
                            onDelete = onDelete
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // App Telemetry Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(ArkPaper)
                    .border(1.dp, ArkBorder, RoundedCornerShape(6.dp))
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
                        color = ArkMuted
                    )
                    Text(
                        text = "v${instance.realVersionName} (${instance.realApkSizeBytes / (1024 * 1024)} MB)",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = ArkInk
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArkAction)
                        .clickable { onLaunchRealAppDirectly() }
                        .padding(vertical = 10.dp)
                        .testTag("launch_button_${instance.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Launch",
                            tint = ArkPaper,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LAUNCH",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = ArkPaper
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArkSurface)
                        .border(1.dp, ArkBorder, RoundedCornerShape(8.dp))
                        .clickable { onLaunch() }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .testTag("details_button_${instance.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "DETAILS",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = ArkSoft
                    )
                }
            }
        }
    }
}
