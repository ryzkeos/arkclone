package com.arkeos.arkclone.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.engine.RealAppIconView
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InstanceGridItem(
    instance: ClonedInstance,
    onLaunchRealAppDirectly: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .combinedClickable(
                    onClick = onLaunchRealAppDirectly,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showMenu = true
                    }
                )
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .testTag("instance_grid_${instance.id}"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RealAppIconView(
                packageName = instance.packageName,
                appName = instance.appName,
                size = 56.dp,
                shapeRadius = 12.dp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = instance.alias,
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = ArkInk,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
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
