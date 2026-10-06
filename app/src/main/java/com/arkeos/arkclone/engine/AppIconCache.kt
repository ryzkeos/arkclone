package com.arkeos.arkclone.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkSurface
import com.arkeos.arkclone.ui.theme.ArkPaper
import com.arkeos.arkclone.ui.theme.ArkInk

object AppIconCache {
    private val iconMap = mutableMapOf<String, Bitmap?>()

    fun getAppIcon(context: Context, packageName: String): Bitmap? {
        if (iconMap.containsKey(packageName)) {
            return iconMap[packageName]
        }
        val bitmap = try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val drawable = pm.getApplicationIcon(appInfo)
            drawableToBitmap(drawable)
        } catch (e: Exception) {
            null
        }
        iconMap[packageName] = bitmap
        return bitmap
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96
        val height = if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}

@Composable
fun RealAppIconView(
    packageName: String,
    appName: String,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    shapeRadius: Dp = 8.dp
) {
    val context = LocalContext.current
    val bitmap = remember(packageName) {
        AppIconCache.getAppIcon(context, packageName)
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "$appName real icon",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(shapeRadius))
                .border(1.dp, ArkBorder, RoundedCornerShape(shapeRadius))
        )
    } else {
        // Futuristic Ark monochrome monogram fallback
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(shapeRadius))
                .background(ArkSurface)
                .border(1.dp, ArkBorder, RoundedCornerShape(shapeRadius)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = appName.take(2).uppercase(),
                fontFamily = ChakraPetchFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.38f).sp,
                color = ArkInk
            )
        }
    }
}
