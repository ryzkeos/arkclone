package com.arkeos.arkclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkSurfaceVariant
import com.arkeos.arkclone.ui.theme.ArkMuted
import com.arkeos.arkclone.ui.theme.ArkSubtle
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkSoft

@Composable
fun ResourceMetricHud(
    installedCount: Int,
    totalInstancesCount: Int,
    freeRamFormatted: String,
    apkSizeFormatted: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ArkSurfaceVariant)
            .border(1.dp, ArkBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = ArkInk,
                    modifier = Modifier.height(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "OVERVIEW",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = ArkInk
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Apps,
                            contentDescription = "Installed clones",
                            tint = ArkSubtle,
                            modifier = Modifier.height(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "INSTALLED",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = ArkMuted
                        )
                    }
                    Text(
                        text = "$installedCount / $totalInstancesCount",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ArkInk
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(ArkBorder)
                )

                Column(
                    modifier = Modifier
                        .weight(1f).padding(start = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Free device memory",
                            tint = ArkSubtle,
                            modifier = Modifier.height(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FREE RAM",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = ArkMuted
                        )
                    }
                    Text(
                        text = freeRamFormatted,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ArkInk
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(ArkBorder)
                )

                Column(
                    modifier = Modifier
                        .weight(1.1f).padding(start = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Clone APK size",
                            tint = ArkSubtle,
                            modifier = Modifier.height(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CLONE SIZE",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = ArkMuted
                        )
                    }
                    Text(
                        text = apkSizeFormatted,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ArkSoft
                    )
                }
            }
        }
    }
}
