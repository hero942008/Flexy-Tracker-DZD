package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.CurrencyMode
import com.example.ui.theme.DjezzyRed
import com.example.ui.theme.ManualBlue
import com.example.ui.theme.MobilisGreen
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.OoredooRuby
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantNetGreen
import com.example.ui.theme.VibrantPeachContainer
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary
import com.example.util.CurrencyFormatter

@Composable
fun AddTransactionDialog(
    defaultPercentage: Double,
    currencyMode: CurrencyMode,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, operator: String, percentage: Double, note: String?) -> Unit
) {
    var amountInput by remember { mutableStateOf("") }
    var isInputInCentimes by remember { mutableStateOf(false) }
    var selectedOperator by remember { mutableStateOf("Mobilis") }
    var percentage by remember { mutableStateOf(defaultPercentage) }
    var noteInput by remember { mutableStateOf("") }

    val rawNumber = amountInput.toDoubleOrNull() ?: 0.0
    val amountInDinar = if (isInputInCentimes) rawNumber / 100.0 else rawNumber

    val cutAmount = (amountInDinar * percentage) / 100.0
    val netAmount = amountInDinar - cutAmount

    val operators = listOf(
        Pair("Mobilis", MobilisGreen),
        Pair("Djezzy", DjezzyRed),
        Pair("Ooredoo", OoredooRuby),
        Pair("يدوي", ManualBlue)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تسجيل فليكسي جديد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Unit Switcher: Dinar vs Centimes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isInputInCentimes = false }
                            .testTag("input_mode_dinar"),
                        color = if (!isInputInCentimes) VibrantPurpleContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "بالدينار (DA)",
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (!isInputInCentimes) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isInputInCentimes) OnVibrantPurpleContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { isInputInCentimes = true }
                            .testTag("input_mode_centimes"),
                        color = if (isInputInCentimes) VibrantPurpleContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "بالسنتيم (مثلاً 100 ألف)",
                            modifier = Modifier.padding(vertical = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (isInputInCentimes) FontWeight.Bold else FontWeight.Normal,
                            color = if (isInputInCentimes) OnVibrantPurpleContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount Text Field
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = {
                        Text(
                            if (isInputInCentimes) "أدخل المبلغ بالسنتيم (مثلاً: 100000)" else "أدخل المبلغ بالدينار (مثلاً: 1000)"
                        )
                    },
                    placeholder = { Text(if (isInputInCentimes) "100000" else "1000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Operator Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "الشريحة / المتعامل:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        operators.forEach { (name, color) ->
                            val isSelected = selectedOperator == name
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { selectedOperator = name }
                                    .testTag("operator_select_$name"),
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        text = name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Live Net Preview Card
                if (amountInDinar > 0) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = VibrantPeachContainer.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "المبلغ الإجمالي:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = CurrencyFormatter.formatDinar(amountInDinar),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "خصم النسبة (${percentage.toInt()}%):",
                                    fontSize = 12.sp,
                                    color = VibrantFeeRed
                                )
                                Text(
                                    text = "-${CurrencyFormatter.formatDinar(cutAmount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantFeeRed
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "الصافي النهائي:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantNetGreen
                                )
                                Text(
                                    text = CurrencyFormatter.formatDinar(netAmount),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = VibrantNetGreen
                                )
                            }
                            Text(
                                text = "بالسنتيم: ${CurrencyFormatter.getAlgerianSpokenLabel(netAmount)}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Optional Note
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("ملاحظة (اختياري)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountInDinar > 0) {
                        onConfirm(amountInDinar, selectedOperator, percentage, noteInput.ifBlank { null })
                    }
                },
                enabled = amountInDinar > 0,
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurplePrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_add_transaction_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text("حفظ واقتطاع الصافي")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

