package com.arkeos.arkclone.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.window.Dialog
import com.arkeos.arkclone.engine.RealAppIconView
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.RealInstalledApk
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkSurface
import com.arkeos.arkclone.ui.theme.ArkSurfaceVariant
import com.arkeos.arkclone.ui.theme.ArkMuted
import com.arkeos.arkclone.ui.theme.ArkSubtle
import com.arkeos.arkclone.ui.theme.ArkPaper
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkSoft

private const val MAX_CLONES = 5

@Composable
fun CloneAppDialog(
    availableApps: List<RealInstalledApk>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onConfirmClone: (
        targetApk: RealInstalledApk,
        count: Int,
        aliases: List<String>,
        category: AppCategory
    ) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<RealInstalledApk?>(availableApps.firstOrNull()) }
    var cloneCount by remember { mutableIntStateOf(1) }
    val nicknames = remember { mutableStateListOf("") }
    var selectedCategory by remember { mutableStateOf(AppCategory.TOOLS) }

    val filteredApps = remember(searchQuery, availableApps) {
        if (searchQuery.isBlank()) availableApps
        else availableApps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    val selectableCategories = listOf(
        AppCategory.SOCIAL,
        AppCategory.MESSAGING,
        AppCategory.TOOLS,
        AppCategory.GAMES
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ArkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .border(1.dp, ArkInk, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CLONE APPLICATION",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp,
                            color = ArkInk
                        )
                        Text(
                            text = "Select an app to clone",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            color = ArkSubtle
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ArkInk,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search installed apps...",
                            fontFamily = ChakraPetchFontFamily,
                            fontSize = 11.sp,
                            color = ArkMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = ArkSubtle,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArkInk,
                        unfocusedBorderColor = ArkBorder,
                        focusedTextColor = ArkInk,
                        unfocusedTextColor = ArkInk,
                        focusedContainerColor = ArkSurfaceVariant,
                        unfocusedContainerColor = ArkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Installed Apps List
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INSTALLED APPS (${filteredApps.size})",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = ArkMuted
                    )

                    if (isScanning) {
                        CircularProgressIndicator(
                            color = ArkInk,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ArkPaper)
                        .border(1.dp, ArkBorder, RoundedCornerShape(8.dp))
                        .padding(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredApps) { app ->
                        val isSelected = selectedApp?.packageName == app.packageName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ArkSurfaceVariant else ArkPaper)
                                .border(
                                    1.dp,
                                    if (isSelected) ArkInk else ArkBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedApp = app
                                    selectedCategory = app.category
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("pick_apk_${app.packageName}"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RealAppIconView(
                                    packageName = app.packageName,
                                    appName = app.appName,
                                    size = 32.dp,
                                    shapeRadius = 6.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = ArkInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${app.packageName} • v${app.versionName}",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 9.sp,
                                        color = ArkSubtle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isSelected) ArkInk else ArkSurface)
                                        .border(1.dp, ArkBorder, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (app.isSystemApp) "SYSTEM" else "INSTALLED",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 8.sp,
                                        color = if (isSelected) ArkPaper else ArkSoft
                                    )
                                }
                                Text(
                                    text = "${app.apkSizeBytes / (1024 * 1024)} MB",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontSize = 8.sp,
                                    color = ArkMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (cloneCount > 1) "CLONE NICKNAMES" else "CLONE NICKNAME",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = ArkMuted
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(ArkSurfaceVariant)
                            .border(1.dp, ArkBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (cloneCount > 1) {
                                    cloneCount--
                                    nicknames.removeAt(nicknames.lastIndex)
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Fewer clones",
                                tint = ArkInk,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = cloneCount.toString(),
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = ArkInk,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = {
                                if (cloneCount < MAX_CLONES) {
                                    cloneCount++
                                    nicknames.add("")
                                }
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "More clones",
                                tint = ArkInk,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 108.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    nicknames.forEachIndexed { index, nickname ->
                        OutlinedTextField(
                            value = nickname,
                            onValueChange = { nicknames[index] = it },
                            singleLine = true,
                            placeholder = {
                                Text(
                                    if (cloneCount > 1) "Clone ${index + 1} nickname" else "Clone nickname",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontSize = 11.sp,
                                    color = ArkMuted
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArkInk,
                                unfocusedBorderColor = ArkBorder,
                                focusedTextColor = ArkInk,
                                unfocusedTextColor = ArkInk,
                                focusedContainerColor = ArkSurfaceVariant,
                                unfocusedContainerColor = ArkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "CATEGORY",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = ArkMuted
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    selectableCategories.forEach { category ->
                        val isCatSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCatSelected) ArkInk else ArkSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isCatSelected) ArkInk else ArkBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { selectedCategory = category }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category.title.uppercase(),
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = if (isCatSelected) ArkPaper else ArkInk
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Confirm Clone Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedApp != null) ArkInk else ArkMuted)
                        .clickable(enabled = selectedApp != null) {
                            selectedApp?.let { app ->
                                onConfirmClone(
                                    app,
                                    cloneCount,
                                    nicknames.toList(),
                                    selectedCategory
                                )
                            }
                        }
                        .padding(vertical = 12.dp)
                        .testTag("confirm_clone_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = ArkPaper,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CREATE CLONED APP",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = ArkPaper
                        )
                    }
                }
            }
        }
    }
}
