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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.FlexyTransaction
import com.example.data.repository.CurrencyMode
import com.example.ui.theme.DjezzyRed
import com.example.ui.theme.ManualBlue
import com.example.ui.theme.MobilisGreen
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.OoredooRuby
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantNetGreen
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary
import com.example.ui.theme.VibrantSurfaceVariant2
import com.example.util.CurrencyFormatter

@Composable
fun TransactionItem(
    transaction: FlexyTransaction,
    currencyMode: CurrencyMode,
    onDelete: (FlexyTransaction) -> Unit,
    modifier: Modifier = Modifier
) {
    val operatorBadgeColor = when (transaction.operatorName.lowercase()) {
        "mobilis" -> MobilisGreen
        "djezzy" -> DjezzyRed
        "ooredoo" -> OoredooRuby
        else -> ManualBlue
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Icon + Operator Info & Gross
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(VibrantSurfaceVariant2),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (transaction.isAutoDetected) Icons.Default.Smartphone else Icons.Default.TouchApp,
                            contentDescription = transaction.operatorName,
                            tint = OnVibrantPurpleContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "+ ${CurrencyFormatter.formatAmount(transaction.amount, currencyMode)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (transaction.isAutoDetected) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = VibrantPurpleContainer
                                ) {
                                    Text(
                                        text = "SMS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnVibrantPurpleContainer,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${transaction.operatorName} • ${CurrencyFormatter.formatDateTime(transaction.timestamp)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right side: Net + Fee breakdown + Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = CurrencyFormatter.formatAmount(transaction.netAmount, currencyMode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantNetGreen
                        )
                        Text(
                            text = "-${CurrencyFormatter.formatAmount(transaction.cutAmount, currencyMode)} (${transaction.cutPercentage.toInt()}%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = VibrantFeeRed
                        )
                    }

                    IconButton(
                        onClick = { onDelete(transaction) },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_transaction_${transaction.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف المعاملة",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Optional spoken label in centimes & note
            if (currencyMode == CurrencyMode.DINAR || !transaction.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (!transaction.note.isNullOrBlank()) "ملاحظة: ${transaction.note}" else "بالسنتيم: ${CurrencyFormatter.getAlgerianSpokenLabel(transaction.amount)} ➔ صافي: ${CurrencyFormatter.getAlgerianSpokenLabel(transaction.netAmount)}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

