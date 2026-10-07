package com.arkeos.arkclone.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkMuted
import com.arkeos.arkclone.ui.theme.ArkSurface
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily

@Composable
fun InstanceActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onLaunchRealAppDirectly: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onDelete: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(ArkSurface)
            .border(1.dp, ArkBorder)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    "Launch Application",
                    fontFamily = ChakraPetchFontFamily,
                    color = ArkInk,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = ArkInk,
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = {
                onDismiss()
                onLaunchRealAppDirectly()
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    "App Info (Settings)",
                    fontFamily = ChakraPetchFontFamily,
                    color = ArkInk,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = ArkInk,
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = {
                onDismiss()
                onOpenAppSettings()
            }
        )

        DropdownMenuItem(
            text = {
                Text(
                    "Delete Clone",
                    fontFamily = ChakraPetchFontFamily,
                    color = ArkInk,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = ArkMuted,
                    modifier = Modifier.size(16.dp)
                )
            },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}
