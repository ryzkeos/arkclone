package com.example.ui.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.RealAppIconView
import com.example.model.AppCategory
import com.example.model.CacheCleanResult
import com.example.model.ClonedInstance
import com.example.model.InstanceStatus
import com.example.model.RealInstalledApk
import com.example.ui.components.DashboardTopBar
import com.example.ui.components.InstanceCard
import com.example.ui.components.OneTapCleanCard
import com.example.ui.components.ResourceMetricHud
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GrayMedium
import com.example.ui.theme.GraySubtle
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverAccent

@Composable
fun DashboardScreen(
    instances: List<ClonedInstance>,
    installedApps: List<RealInstalledApk>,
    unreadCount: Int,
    isDarkMode: Boolean,
    isEcoActive: Boolean,
    isCleaningCache: Boolean,
    cleanResult: CacheCleanResult?,
    selectedCategory: AppCategory,
    totalCacheFormatted: String,
    totalMemoryMb: Float,
    onToggleDarkMode: () -> Unit,
    onCategorySelect: (AppCategory) -> Unit,
    onOpenCloneDialog: () -> Unit,
    onOpenNotificationCenter: () -> Unit,
    onOpenResourceLimiter: () -> Unit,
    onOneTapCleanCache: () -> Unit,
    onDismissCleanResult: () -> Unit,
    onQuickClone: (RealInstalledApk) -> Unit,
    onLaunchInstance: (String) -> Unit,
    onLaunchRealAppDirectly: (String) -> Unit,
    onOpenAppSettings: (String) -> Unit,
    onToggleSleep: (String) -> Unit,
    onClearInstanceCache: (String) -> Unit,
    onSimulateNotification: (String) -> Unit,
    onDeleteInstance: (String) -> Unit
) {
    val activeCount = instances.count { it.status == InstanceStatus.RUNNING }

    val filteredInstances = if (selectedCategory == AppCategory.ALL) {
        instances
    } else {
        instances.filter { it.category == selectedCategory }
    }

    Scaffold(
        containerColor = PureBlack,
        topBar = {
            DashboardTopBar(
                unreadNotificationsCount = unreadCount,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onNotificationsClick = onOpenNotificationCenter,
                onResourceLimiterClick = onOpenResourceLimiter
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenCloneDialog,
                containerColor = PureWhite,
                contentColor = PureBlack,
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
            // HUD telemetry banner
            item {
                ResourceMetricHud(
                    activeCount = activeCount,
                    totalInstancesCount = instances.size,
                    totalMemoryMb = totalMemoryMb,
                    totalCacheFormatted = totalCacheFormatted,
                    isEcoActive = isEcoActive
                )
            }

            // One-Tap Cache Cleaner Section
            item {
                OneTapCleanCard(
                    totalCacheFormatted = totalCacheFormatted,
                    isCleaning = isCleaningCache,
                    lastResult = cleanResult,
                    onCleanAllClick = onOneTapCleanCache,
                    onDismissResult = onDismissCleanResult
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
                                    .background(if (isCatSelected) PureWhite else DarkSurface)
                                    .border(
                                        1.dp,
                                        if (isCatSelected) PureWhite else DarkCardBorder,
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
                                    color = if (isCatSelected) PureBlack else PureWhite
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
                                    tint = PureWhite,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SUGGESTED APPS",
                                    fontFamily = ChakraPetchFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    letterSpacing = 1.sp,
                                    color = PureWhite
                                )
                            }

                            Text(
                                text = "INSTALLED ON DEVICE",
                                fontFamily = ChakraPetchFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 9.sp,
                                color = GrayMedium
                            )
                        }

                        Text(
                            text = "Tap any installed app below to immediately clone and isolate its data:",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = GraySubtle,
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
                            .background(DarkSurfaceVariant)
                            .border(1.dp, DarkCardBorder, RoundedCornerShape(10.dp))
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

                                Column {
                                    Text(
                                        text = app.appName,
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PureWhite
                                    )
                                    Text(
                                        text = "${app.packageName} • ${app.apkSizeBytes / (1024 * 1024)} MB",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 10.sp,
                                        color = GraySubtle
                                    )
                                }
                            }

                            // 1-Tap Quick Clone Button
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(PureWhite)
                                    .clickable { onQuickClone(app) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("quick_clone_btn_${app.packageName}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Clone",
                                        tint = PureBlack,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "CLONE",
                                        fontFamily = ChakraPetchFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp,
                                        color = PureBlack
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
                            color = GraySubtle
                        )

                        Text(
                            text = "ISOLATED DATA",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = GrayMedium
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
                            onToggleSleep = { onToggleSleep(instance.id) },
                            onClearCache = { onClearInstanceCache(instance.id) },
                            onSimulateNotification = { onSimulateNotification(instance.id) },
                            onOpenAppSettings = { onOpenAppSettings(instance.packageName) },
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
                                color = GrayMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(installedApps.take(6)) { app ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkSurfaceVariant)
                                            .border(1.dp, DarkCardBorder, RoundedCornerShape(8.dp))
                                            .clickable { onQuickClone(app) }
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
                                                color = PureWhite
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
