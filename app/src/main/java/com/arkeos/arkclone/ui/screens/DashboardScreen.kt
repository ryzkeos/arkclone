package com.arkeos.arkclone.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.arkeos.arkclone.model.AppCategory
import com.arkeos.arkclone.model.ClonedInstance
import com.arkeos.arkclone.model.RealInstalledApk
import com.arkeos.arkclone.ui.components.DashboardTopBar
import com.arkeos.arkclone.ui.components.InstanceCard
import com.arkeos.arkclone.ui.components.ResourceMetricHud
import com.arkeos.arkclone.ui.theme.ChakraPetchFontFamily
import com.arkeos.arkclone.ui.theme.ArkBorder
import com.arkeos.arkclone.ui.theme.ArkSurface
import com.arkeos.arkclone.ui.theme.ArkSurfaceVariant
import com.arkeos.arkclone.ui.theme.ArkMuted
import com.arkeos.arkclone.ui.theme.ArkSubtle
import com.arkeos.arkclone.ui.theme.ArkPaper
import com.arkeos.arkclone.ui.theme.ArkInk
import com.arkeos.arkclone.ui.theme.ArkSoft

@Composable
fun DashboardScreen(
    instances: List<ClonedInstance>,
    installedApps: List<RealInstalledApk>,
    isDarkMode: Boolean,
    selectedCategory: AppCategory,
    installedCount: Int,
    freeRamFormatted: String,
    apkSizeFormatted: String,
    onToggleDarkMode: () -> Unit,
    onCategorySelect: (AppCategory) -> Unit,
    onOpenCloneDialog: () -> Unit,
    onSuggestionClick: (RealInstalledApk) -> Unit,
    onLaunchInstance: (String) -> Unit,
    onLaunchRealAppDirectly: (String) -> Unit,
    onOpenAppSettings: (String) -> Unit,
    onDeleteInstance: (String) -> Unit
) {
    val filteredInstances = if (selectedCategory == AppCategory.ALL) {
        instances
    } else {
        instances.filter { it.category == selectedCategory }
    }

    Scaffold(
        containerColor = ArkPaper,
        topBar = {
            DashboardTopBar(
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenCloneDialog,
                containerColor = ArkInk,
                contentColor = ArkPaper,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("fab_clone_app")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Clone App",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CLONE APP",
                    fontFamily = ChakraPetchFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            item {
                ResourceMetricHud(
                    installedCount = installedCount,
                    totalInstancesCount = instances.size,
                    freeRamFormatted = freeRamFormatted,
                    apkSizeFormatted = apkSizeFormatted
                )
            }

            // Category Filter Row
            if (instances.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(AppCategory.values()) { cat ->
                            val isCatSelected = cat == selectedCategory
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCatSelected) ArkInk else ArkSurface)
                                    .border(
                                        1.dp,
                                        if (isCatSelected) ArkInk else ArkBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { onCategorySelect(cat) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("category_tab_${cat.name}")
                            ) {
                                Text(
                                    text = cat.title.uppercase(),
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.5.sp,
                                    color = if (isCatSelected) ArkPaper else ArkInk
                                )
                            }
                        }
                    }
                }
            }

            // If no cloned instances yet, show Suggestions of apps to clone
            if (filteredInstances.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ArkInk,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SUGGESTED APPS",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp,
                                    color = ArkInk
                                )
                            }

                            Text(
                                text = "INSTALLED ON DEVICE",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                color = ArkMuted
                            )
                        }

                        Text(
                            text = "Tap an app below to clone it:",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = ArkSubtle,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                    }
                }

                // List of suggested applications
                val suggestions = installedApps.take(8)
                items(
                    items = suggestions,
                    key = { it.packageName }
                ) { app ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ArkSurfaceVariant)
                            .border(1.dp, ArkBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                            .testTag("suggested_app_${app.packageName}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                    size = 38.dp,
                                    shapeRadius = 8.dp
                                )

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = app.appName,
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = ArkInk,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${app.packageName} • ${app.apkSizeBytes / (1024 * 1024)} MB",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        color = ArkSubtle,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 1-Tap Quick Clone Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArkInk)
                                    .clickable { onSuggestionClick(app) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("quick_clone_btn_${app.packageName}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Clone",
                                        tint = ArkPaper,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "CLONE",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = ArkPaper
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Section Header for Cloned Instances
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CLONED APPS (${filteredInstances.size})",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = ArkSubtle
                        )
                    }
                }

                // Render existing Cloned Instances
                items(
                    items = filteredInstances,
                    key = { it.id }
                ) { instance ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                        InstanceCard(
                            instance = instance,
                            onLaunch = { onLaunchInstance(instance.id) },
                            onLaunchRealAppDirectly = { onLaunchRealAppDirectly(instance.id) },
                            onOpenAppSettings = { onOpenAppSettings(instance.clonePackageName.ifBlank { instance.packageName }) },
                            onDelete = { onDeleteInstance(instance.id) }
                        )
                    }
                }

                // Suggestions footer row if user already has some clones
                if (installedApps.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Text(
                                text = "MORE SUGGESTIONS",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp,
                                color = ArkMuted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(installedApps.take(6)) { app ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ArkSurfaceVariant)
                                            .border(1.dp, ArkBorder, RoundedCornerShape(8.dp))
                                            .clickable { onSuggestionClick(app) }
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RealAppIconView(
                                                packageName = app.packageName,
                                                appName = app.appName,
                                                size = 24.dp,
                                                shapeRadius = 4.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "+ ${app.appName}",
                                                fontFamily = ChakraPetchFontFamily,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 11.sp,
                                                color = ArkInk,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
