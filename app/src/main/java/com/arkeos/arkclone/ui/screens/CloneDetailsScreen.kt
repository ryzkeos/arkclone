package com.arkeos.arkclone.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.DarkCardBorder
import com.arkeos.arkclone.ui.theme.DarkSurface
import com.arkeos.arkclone.ui.theme.DarkSurfaceVariant
import com.arkeos.arkclone.ui.theme.GrayMedium
import com.arkeos.arkclone.ui.theme.GraySubtle
import com.arkeos.arkclone.ui.theme.PureBlack
import com.arkeos.arkclone.ui.theme.PureWhite

@Composable
fun CloneDetailsScreen(
    instance: ClonedInstance,
    onExit: () -> Unit,
    onLaunch: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    BackHandler { onExit() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .statusBarsPadding()
                .border(1.dp, DarkCardBorder)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("exit_details_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to dashboard",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            RealAppIconView(
                packageName = instance.packageName,
                appName = instance.appName,
                size = 32.dp,
                shapeRadius = 6.dp
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${instance.appName} #${instance.cloneNumber}",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = instance.alias,
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = GraySubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PureWhite)
                        .clickable { onLaunch() }
                        .padding(16.dp)
                        .testTag("details_launch_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = PureBlack,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LAUNCH",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = PureBlack
                        )
                    }
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onOpenAppSettings() }
                        .padding(12.dp)
                        .testTag("details_app_settings_button"),
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
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "DETAILS",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = PureWhite
                    )
                    DetailRow("Status", instance.installState.label)
                    DetailRow("Clone package", instance.clonePackageName.ifBlank { "Not set" })
                    DetailRow("Original package", instance.packageName)
                    DetailRow("Version", "v${instance.realVersionName} (code ${instance.realVersionCode})")
                    DetailRow("Target SDK", "API ${instance.targetSdkVersion}")
                    DetailRow("APK size", "${instance.realApkSizeBytes / (1024 * 1024)} MB")
                    if (instance.installError.isNotBlank()) {
                        DetailRow("Error", instance.installError)
                    }
                    if (instance.realSourceApkPath.isNotBlank()) {
                        DetailRow("Source APK", instance.realSourceApkPath)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label.uppercase(),
            fontFamily = ChakraPetchFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 9.sp,
            color = GrayMedium
        )
        Text(
            text = value,
            fontFamily = ChakraPetchFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = PureWhite,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
