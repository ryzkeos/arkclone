package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChakraPetchFontFamily
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GrayMedium
import com.example.ui.theme.GraySubtle
import com.example.ui.theme.PureBlack
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SilverAccent

@Composable
fun ResourceMetricHud(
    activeCount: Int,
    totalInstancesCount: Int,
    totalMemoryMb: Float,
    totalCacheFormatted: String,
    isEcoActive: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVariant)
            .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Architecture Isolation",
                        tint = PureWhite,
                        modifier = Modifier.height(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VIRTUAL ENCLAVE TELEMETRY",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        letterSpacing = 1.2.sp,
                        color = PureWhite
                    )
                }

                // Low-Resource badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isEcoActive) PureWhite else GrayMedium)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isEcoActive) "LOW OVERHEAD" else "MAX PERF",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp,
                        color = PureBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Metric 1: Active Instances
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "INSTANCES",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 10.sp,
                        color = GrayMedium
                    )
                    Text(
                        text = "$activeCount / $totalInstancesCount",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PureWhite
                    )
                    Text(
                        text = "Running Spaces",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        color = GraySubtle
                    )
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(DarkCardBorder)
                )

                // Metric 2: Virtual RAM
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "RAM",
                            tint = GraySubtle,
                            modifier = Modifier.height(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "V-RAM",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = GrayMedium
                        )
                    }
                    Text(
                        text = String.format("%.1f MB", totalMemoryMb),
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PureWhite
                    )
                    Text(
                        text = "Low Footprint",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        color = GraySubtle
                    )
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(38.dp)
                        .background(DarkCardBorder)
                )

                // Metric 3: Total Cache Junk
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .padding(start = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = "Cache",
                            tint = GraySubtle,
                            modifier = Modifier.height(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "CACHE JUNK",
                            fontFamily = ChakraPetchFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp,
                            color = GrayMedium
                        )
                    }
                    Text(
                        text = totalCacheFormatted,
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SilverAccent
                    )
                    Text(
                        text = "One-Tap Flushable",
                        fontFamily = ChakraPetchFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        color = GraySubtle
                    )
                }
            }
        }
    }
}
