package com.arkeos.arkclone.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.model.CacheCleanResult
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.DarkCardBorder
import com.arkeos.arkclone.ui.theme.DarkSurfaceVariant
import com.arkeos.arkclone.ui.theme.GrayMedium
import com.arkeos.arkclone.ui.theme.GraySubtle
import com.arkeos.arkclone.ui.theme.PureBlack
import com.arkeos.arkclone.ui.theme.PureWhite
import com.arkeos.arkclone.ui.theme.SilverAccent

@Composable
fun OneTapCleanCard(
    totalCacheFormatted: String,
    isCleaning: Boolean,
    lastResult: CacheCleanResult?,
    onCleanAllClick: () -> Unit,
    onDismissResult: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cleaningRotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, if (isCleaning) PureWhite else DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = "One Tap Clean",
                            tint = PureWhite,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ONE-TAP INSTANCE CACHE CLEAN",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = PureWhite
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isCleaning) {
                            "Flushing isolated temp buffers & reclaiming blocks..."
                        } else {
                            "$totalCacheFormatted temporary files ready for purge across all spaces."
                        },
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = GraySubtle
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // High Contrast Action Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isCleaning) GrayMedium else PureWhite)
                        .clickable(enabled = !isCleaning) { onCleanAllClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("one_tap_clean_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isCleaning) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(14.dp)
                                    .rotate(rotation),
                                color = PureBlack,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "PURGING",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = PureBlack
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Purge Cache",
                                tint = PureBlack,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CLEAN ALL",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = PureBlack
                            )
                        }
                    }
                }
            }

            // Results notification pill if recently cleaned
            if (lastResult != null && !isCleaning) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(PureBlack)
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onDismissResult() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Success",
                            tint = PureWhite,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Purged ${(lastResult.totalCleanedBytes / (1024 * 1024))} MB across ${lastResult.instancesCleanedCount} cloned enclaves.",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = SilverAccent
                        )
                    }
                    Text(
                        text = "DISMISS",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = GrayMedium
                    )
                }
            }
        }
    }
}
