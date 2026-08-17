package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.CurrencyMode
import com.example.ui.theme.OnVibrantPeach
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.VibrantBorder
import com.example.ui.theme.VibrantDarkPeachContainer
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantNetGreen
import com.example.ui.theme.VibrantPeachContainer
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary
import com.example.ui.viewmodel.FlexySummary
import com.example.util.CurrencyFormatter

@Composable
fun SummaryCards(
    summary: FlexySummary,
    currencyMode: CurrencyMode,
    cutPercentage: Double,
    filterName: String,
    onToggleCurrency: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val heroBgColor = if (isDark) VibrantDarkPeachContainer else VibrantPeachContainer
    val heroTextColor = if (isDark) Color(0xFFFFDBCF) else OnVibrantPeach

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Card: The Net Profit / "الصافي النهائي" in Vibrant Peach Container
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("net_summary_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = heroBgColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Top Row: Title, Subtitle, Currency toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(VibrantPurplePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "الصافي النهائي ($filterName)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = heroTextColor.copy(alpha = 0.85f),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "بعد اقتطاع ${cutPercentage.toInt()}%",
                                fontSize = 11.sp,
                                color = heroTextColor.copy(alpha = 0.65f)
                            )
                        }
                    }

                    // Currency Mode Toggle Chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onToggleCurrency() }
                            .testTag("currency_toggle_chip"),
                        color = heroTextColor.copy(alpha = 0.12f),
                        contentColor = heroTextColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "تبديل العملة",
                                modifier = Modifier.size(16.dp),
                                tint = heroTextColor
                            )
                            Text(
                                text = if (currencyMode == CurrencyMode.DINAR) "دج (دينار)" else "سنتيم",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = heroTextColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Net Big Display Typography
                AnimatedContent(
                    targetState = summary.totalNet,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "netAmountAnim"
                ) { targetNet ->
                    Column {
                        Text(
                            text = CurrencyFormatter.formatAmount(targetNet, currencyMode),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            color = heroTextColor,
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "≈ ${CurrencyFormatter.getAlgerianSpokenLabel(targetNet)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Sub-stats grid inside the hero card
                HorizontalDivider(
                    color = heroTextColor.copy(alpha = 0.12f),
                    thickness = 1.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Gross
                    Column {
                        Text(
                            text = "المدخول الإجمالي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = heroTextColor.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = CurrencyFormatter.formatAmount(summary.totalGross, currencyMode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = heroTextColor
                        )
                    }

                    // Fee / Cut
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.clickable { onOpenSettings() }
                    ) {
                        Text(
                            text = "الخصم (${cutPercentage.toInt()}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = heroTextColor.copy(alpha = 0.65f)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "- ${CurrencyFormatter.formatAmount(summary.totalCut, currencyMode)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantFeeRed
                        )
                    }
                }
            }
        }

        // Quick Sub-info chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(VibrantPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = OnVibrantPurpleContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "عدد العمليات",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${summary.count} عملية فليكسي",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenSettings() },
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(VibrantPurpleContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Percent,
                            contentDescription = null,
                            tint = OnVibrantPurpleContainer,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "نسبة الاقتطاع",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${cutPercentage.toInt()}% مخصومة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantFeeRed
                        )
                    }
                }
            }
        }
    }
}

