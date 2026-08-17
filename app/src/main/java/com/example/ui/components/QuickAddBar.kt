package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary

data class QuickPreset(
    val amount: Double,
    val labelAr: String,
    val centimesLabel: String
)

val QUICK_PRESETS = listOf(
    QuickPreset(100.0, "100 دج", "10 آلاف"),
    QuickPreset(200.0, "200 دج", "20 ألف"),
    QuickPreset(500.0, "500 دج", "50 ألف"),
    QuickPreset(1000.0, "1000 دج", "100 ألف"),
    QuickPreset(2000.0, "2000 دج", "200 ألف"),
    QuickPreset(3000.0, "3000 دج", "300 ألف"),
    QuickPreset(5000.0, "5000 دج", "500 ألف")
)

@Composable
fun QuickAddBar(
    onQuickAdd: (Double) -> Unit,
    onCustomAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = null,
                    tint = VibrantPurplePrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "إضافة سريعة بنقرة واحدة",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "صافي -10%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = VibrantFeeRed
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Custom Add Button
            OutlinedButton(
                onClick = onCustomAdd,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = VibrantPurplePrimary
                ),
                border = BorderStroke(1.dp, VibrantPurplePrimary),
                modifier = Modifier.testTag("custom_add_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "مبلغ مخصص",
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "مبلغ مخصص",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Preset Buttons
            QUICK_PRESETS.forEach { preset ->
                ElevatedButton(
                    onClick = { onQuickAdd(preset.amount) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.testTag("quick_add_${preset.amount.toInt()}")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "+${preset.labelAr}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "(${preset.centimesLabel})",
                            fontSize = 10.sp,
                            color = VibrantPurplePrimary
                        )
                    }
                }
            }
        }
    }
}

