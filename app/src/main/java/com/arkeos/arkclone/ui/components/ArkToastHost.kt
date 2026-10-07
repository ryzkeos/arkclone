package com.arkeos.arkclone.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.model.ArkToast
import com.arkeos.arkclone.model.ToastType
import com.arkeos.arkclone.ui.theme.ArkBorderStrong
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkPaper
import com.arkeos.arkclone.ui.theme.ArkSurfaceVariant
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MS = 3200L

@Composable
fun ArkToastHost(
    toast: ArkToast?,
    onDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var shown by remember { mutableStateOf<ArkToast?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(toast?.id) {
        if (toast == null) {
            visible = false
        } else {
            shown = toast
            visible = true
            delay(TOAST_DURATION_MS)
            visible = false
            onDismiss(toast.id)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(animationSpec = tween(220)) { it } + fadeIn(tween(220)),
            exit = slideOutVertically(animationSpec = tween(200)) { it } + fadeOut(tween(200))
        ) {
            shown?.let { current ->
                ToastCard(
                    toast = current,
                    onClick = {
                        visible = false
                        onDismiss(current.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ToastCard(
    toast: ArkToast,
    onClick: () -> Unit
) {
    val icon: ImageVector = when (toast.type) {
        ToastType.SUCCESS -> Icons.Default.Check
        ToastType.ERROR -> Icons.Default.Close
        ToastType.INFO -> Icons.Default.Info
    }
    val isError = toast.type == ToastType.ERROR

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ArkSurfaceVariant)
            .border(
                1.dp,
                if (isError) ArkInk else ArkBorderStrong,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("ark_toast"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(ArkInk),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = when (toast.type) {
                    ToastType.SUCCESS -> "Success"
                    ToastType.ERROR -> "Error"
                    ToastType.INFO -> "Info"
                },
                tint = ArkPaper,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = toast.message,
            fontFamily = ChakraPetchFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = ArkInk,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
