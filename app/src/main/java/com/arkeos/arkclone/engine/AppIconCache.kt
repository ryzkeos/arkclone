package com.arkeos.arkclone.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkSurface
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

object AppIconCache {
    private const val ICON_SIZE_PX = 128
    private const val MAX_ENTRIES = 160

    private val icons = LruCache<String, Bitmap>(MAX_ENTRIES)
    private val missing: MutableSet<String> = Collections.synchronizedSet(HashSet())

    fun peek(packageName: String): Bitmap? = icons.get(packageName)

    fun isMissing(packageName: String): Boolean = packageName in missing

    fun getAppIcon(context: Context, packageName: String): Bitmap? {
        icons.get(packageName)?.let { return it }
        if (packageName in missing) return null

        val bitmap = try {
            drawableToBitmap(context.packageManager.getApplicationIcon(packageName))
        } catch (e: Exception) {
            null
        }

        if (bitmap != null) icons.put(packageName, bitmap) else missing.add(packageName)
        return bitmap
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(ICON_SIZE_PX, ICON_SIZE_PX, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, ICON_SIZE_PX, ICON_SIZE_PX)
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
    var bitmap by remember(packageName) { mutableStateOf(AppIconCache.peek(packageName)) }
    var failed by remember(packageName) { mutableStateOf(AppIconCache.isMissing(packageName)) }

    LaunchedEffect(packageName) {
        if (bitmap == null && !failed) {
            val loaded = withContext(Dispatchers.IO) { AppIconCache.getAppIcon(context, packageName) }
            bitmap = loaded
            failed = loaded == null
        }
    }

    val shape = RoundedCornerShape(shapeRadius)
    val current = bitmap

    if (current != null) {
        val image = remember(current) { current.asImageBitmap() }
        Image(
            bitmap = image,
            contentDescription = "$appName real icon",
            modifier = modifier
                .size(size)
                .clip(shape)
                .border(1.dp, ArkBorder, shape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(shape)
                .background(ArkSurface)
                .border(1.dp, ArkBorder, shape),
            contentAlignment = Alignment.Center
        ) {
            if (failed) {
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
}
