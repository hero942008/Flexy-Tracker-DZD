package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OperatorSenderConfig
import com.example.data.repository.CurrencyMode
import com.example.ui.theme.DjezzyRed
import com.example.ui.theme.MobilisGreen
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.OoredooRuby
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantPeachContainer
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentPercentage: Double,
    currencyMode: CurrencyMode,
    operatorSenders: OperatorSenderConfig,
    onDismiss: () -> Unit,
    onSavePercentage: (Double) -> Unit,
    onSaveOperatorSenders: (String, String, String) -> Unit,
    onResetOperatorSenders: () -> Unit,
    onToggleCurrency: (CurrencyMode) -> Unit,
    onClearAll: () -> Unit
) {
    var selectedPercentage by remember { mutableDoubleStateOf(currentPercentage) }
    var mobilisSenders by remember { mutableStateOf(operatorSenders.mobilisSenders) }
    var djezzySenders by remember { mutableStateOf(operatorSenders.djezzySenders) }
    var ooredooSenders by remember { mutableStateOf(operatorSenders.ooredooSenders) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val quickPercentages = listOf(5.0, 7.0, 8.0, 10.0, 12.0, 15.0, 20.0)

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            title = { Text("تأكيد مسح جميع المعاملات") },
            text = { Text("هل أنت متأكد من رغبتك في حذف كافة سجلات الفليكسي المحفوظة؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAll()
                        showClearConfirmation = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantFeeRed)
                ) {
                    Text("نعم، احذف الكل")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = VibrantPurplePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "إعدادات التطبيق والتعبئة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Percentage Adjustment
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نسبة الاقتطاع المخصومة:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${selectedPercentage.toInt()}%",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = VibrantFeeRed
                        )
                    }

                    Slider(
                        value = selectedPercentage.toFloat(),
                        onValueChange = { selectedPercentage = it.toDouble() },
                        valueRange = 1f..30f,
                        steps = 28,
                        colors = SliderDefaults.colors(
                            thumbColor = VibrantPurplePrimary,
                            activeTrackColor = VibrantPurplePrimary
                        ),
                        modifier = Modifier.testTag("percentage_slider")
                    )

                    // Quick Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        quickPercentages.forEach { p ->
                            val isSelected = selectedPercentage.toInt() == p.toInt()
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPercentage = p },
                                color = if (isSelected) VibrantPurplePrimary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${p.toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Section 2: Currency Mode Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "طريقة عرض المبالغ الافتراضية:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onToggleCurrency(CurrencyMode.DINAR) },
                            color = if (currencyMode == CurrencyMode.DINAR) VibrantPurpleContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "بالدينار (DA)",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = if (currencyMode == CurrencyMode.DINAR) FontWeight.Bold else FontWeight.Normal,
                                color = if (currencyMode == CurrencyMode.DINAR) OnVibrantPurpleContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onToggleCurrency(CurrencyMode.CENTIMES) },
                            color = if (currencyMode == CurrencyMode.CENTIMES) VibrantPurpleContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "بالسنتيم (آلاف / ملايين)",
                                modifier = Modifier.padding(vertical = 10.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                fontWeight = if (currencyMode == CurrencyMode.CENTIMES) FontWeight.Bold else FontWeight.Normal,
                                color = if (currencyMode == CurrencyMode.CENTIMES) OnVibrantPurpleContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 3: Telecom Operator SMS Senders Configuration
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sms,
                                contentDescription = null,
                                tint = VibrantPurplePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "أرقام ومرسلي رسائل التعبئة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = {
                                mobilisSenders = OperatorSenderConfig.DEFAULT_MOBILIS
                                djezzySenders = OperatorSenderConfig.DEFAULT_DJEZZY
                                ooredooSenders = OperatorSenderConfig.DEFAULT_OOREDOO
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("الافتراضية", fontSize = 11.sp)
                        }
                    }

                    Text(
                        text = "حدد أرقام أو أسماء المرسلين لكل متعامل (مفصولة بفاصلة) للتعرف على رصيد الفليكسي تلقائياً عند وصول الرسائل القصيرة:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    // Mobilis
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MobilisGreen.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MobilisGreen)
                                )
                                Text(
                                    text = "موبيليس (Mobilis)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MobilisGreen
                                )
                            }
                            OutlinedTextField(
                                value = mobilisSenders,
                                onValueChange = { mobilisSenders = it },
                                label = { Text("أرقام أو أسماء موبيليس", fontSize = 11.sp) },
                                placeholder = { Text("مثال: 644, 600, Arseli, 0661234567", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("mobilis_senders_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MobilisGreen,
                                    focusedLabelColor = MobilisGreen
                                ),
                                singleLine = false,
                                maxLines = 2
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("644", "600", "Arseli", "Mobilis").forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MobilisGreen.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable {
                                            if (!mobilisSenders.contains(tag, ignoreCase = true)) {
                                                mobilisSenders = if (mobilisSenders.isBlank()) tag else "$mobilisSenders, $tag"
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "+ $tag",
                                            fontSize = 10.sp,
                                            color = MobilisGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Djezzy
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, DjezzyRed.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(DjezzyRed)
                                )
                                Text(
                                    text = "جيزي (Djezzy)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DjezzyRed
                                )
                            }
                            OutlinedTextField(
                                value = djezzySenders,
                                onValueChange = { djezzySenders = it },
                                label = { Text("أرقام أو أسماء جيزي", fontSize = 11.sp) },
                                placeholder = { Text("مثال: 710, 700, Flexy, 0770123456", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("djezzy_senders_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = DjezzyRed,
                                    focusedLabelColor = DjezzyRed
                                ),
                                singleLine = false,
                                maxLines = 2
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("710", "700", "Flexy", "Djezzy").forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = DjezzyRed.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable {
                                            if (!djezzySenders.contains(tag, ignoreCase = true)) {
                                                djezzySenders = if (djezzySenders.isBlank()) tag else "$djezzySenders, $tag"
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "+ $tag",
                                            fontSize = 10.sp,
                                            color = DjezzyRed,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Ooredoo
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, OoredooRuby.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(OoredooRuby)
                                )
                                Text(
                                    text = "أوريدو (Ooredoo)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OoredooRuby
                                )
                            }
                            OutlinedTextField(
                                value = ooredooSenders,
                                onValueChange = { ooredooSenders = it },
                                label = { Text("أرقام أو أسماء أوريدو", fontSize = 11.sp) },
                                placeholder = { Text("مثال: 555, 500, Storm, Maxy, 0550123456", fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("ooredoo_senders_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OoredooRuby,
                                    focusedLabelColor = OoredooRuby
                                ),
                                singleLine = false,
                                maxLines = 2
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("555", "500", "Storm", "Maxy", "Ooredoo").forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = OoredooRuby.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable {
                                            if (!ooredooSenders.contains(tag, ignoreCase = true)) {
                                                ooredooSenders = if (ooredooSenders.isBlank()) tag else "$ooredooSenders, $tag"
                                            }
                                        }
                                    ) {
                                        Text(
                                            text = "+ $tag",
                                            fontSize = 10.sp,
                                            color = OoredooRuby,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Info banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = VibrantPeachContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = VibrantPurplePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "مثال: عند دخول فليكسي 100 ألف (1000 دج)، يتم خصم 10% (100 دج) ويصبح الصافي 900 دج (90 ألف).",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Clear all button
                OutlinedButton(
                    onClick = { showClearConfirmation = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VibrantFeeRed
                    ),
                    border = BorderStroke(1.dp, VibrantFeeRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("مسح جميع العمليات")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSavePercentage(selectedPercentage)
                    onSaveOperatorSenders(mobilisSenders, djezzySenders, ooredooSenders)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurplePrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("حفظ التغييرات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}


