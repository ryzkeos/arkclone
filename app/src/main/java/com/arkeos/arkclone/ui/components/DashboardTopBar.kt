package com.arkeos.arkclone.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.R
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.DarkCardBorder
import com.arkeos.arkclone.ui.theme.DarkSurface
import com.arkeos.arkclone.ui.theme.GraySubtle
import com.arkeos.arkclone.ui.theme.PureBlack
import com.arkeos.arkclone.ui.theme.PureWhite
import com.arkeos.arkclone.ui.theme.SilverAccent

@Composable
fun DashboardTopBar(
    unreadNotificationsCount: Int,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNotificationsClick: () -> Unit,
    onResourceLimiterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ark_clone_logo),
                contentDescription = "Ark Clone Logo",
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "ARK CLONE",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp,
                    color = PureWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "VIRTUAL SPACES ONLINE",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = GraySubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Dark Mode Toggle Icon Button (Icon only, consistent with adjacent buttons)
            IconButton(
                onClick = onToggleDarkMode,
                modifier = Modifier.testTag("dark_mode_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = if (isDarkMode) "Switch to Light Mode" else "Switch to Dark Mode",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Notification Bell with Badge
            IconButton(
                onClick = onNotificationsClick,
                modifier = Modifier.testTag("notifications_center_button")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotificationsCount > 0) {
                            Badge(
                                containerColor = PureWhite,
                                contentColor = PureBlack
                            ) {
                                Text(
                                    text = unreadNotificationsCount.toString(),
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notification Center",
                        tint = PureWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Resource Limiter Settings
            IconButton(
                onClick = onResourceLimiterClick,
                modifier = Modifier.testTag("resource_limiter_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Resource Limiter",
                    tint = SilverAccent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
