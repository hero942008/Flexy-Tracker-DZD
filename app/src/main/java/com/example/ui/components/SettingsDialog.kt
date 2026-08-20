package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OperatorSenderConfig
import com.example.data.repository.CurrencyMode
import com.example.ui.theme.MobilisGreen
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantNetGreen
import com.example.ui.theme.VibrantPeachContainer
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary
import com.example.util.CurrencyFormatter

private const val REQUIRED_PASSWORD = "20082008"

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentPercentage: Double,
    currencyMode: CurrencyMode,
    operatorSenders: OperatorSenderConfig,
    onDismiss: () -> Unit,
    onSavePercentage: (Double) -> Unit,
    onSaveOperatorSenders: (String) -> Unit,
    onResetOperatorSenders: () -> Unit,
    onToggleCurrency: (CurrencyMode) -> Unit,
    onClearAll: () -> Unit,
    onAddManualTransaction: (amount: Double, note: String?) -> Unit
) {
    val context = LocalContext.current
    var selectedPercentage by remember { mutableDoubleStateOf(currentPercentage) }
    var mobilisSenders by remember { mutableStateOf(operatorSenders.mobilisSenders) }
    var showClearConfirmation by remember { mutableStateOf(false) }

    // Password & Manual Add State
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var manualAmountText by remember { mutableStateOf("") }
    var manualNoteText by remember { mutableStateOf("") }
    var manualAddSuccessMsg by remember { mutableStateOf<String?>(null) }

    // Battery optimization status
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as? PowerManager }
    var isBatteryIgnored by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && powerManager != null) {
                powerManager.isIgnoringBatteryOptimizations(context.packageName)
            } else {
                true
            }
        )
    }

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

                // Section 3: Battery Optimization Setting (Background continuous operation)
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBatteryIgnored) VibrantNetGreen.copy(alpha = 0.1f) else VibrantPeachContainer.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, if (isBatteryIgnored) VibrantNetGreen.copy(alpha = 0.4f) else VibrantPurplePrimary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("battery_optimization_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isBatteryIgnored) Icons.Default.CheckCircle else Icons.Default.Bolt,
                                contentDescription = null,
                                tint = if (isBatteryIgnored) VibrantNetGreen else VibrantPurplePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "تشغيل التطبيق في الخلفية (توفير البطارية)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = if (isBatteryIgnored) {
                                "✅ تم إيقاف توفير البطارية للتطبيق: يعمل باستمرار في الخلفية لقراءة وتتبع رسائل فليكسي موبيليس فور وصولها."
                            } else {
                                "⚠️ توفير البطارية نشط في النظام: قد يقوم أندرويد بتعليق التطبيق أو تأخير وصول إشعارات الرسائل. يُنصح بإيقاف توفير البطارية لضمان العمل التلقائي الفوري."
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )

                        if (!isBatteryIgnored) {
                            Button(
                                onClick = {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                            context.startActivity(intent)
                                        } else {
                                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            context.startActivity(intent)
                                        }
                                        Toast.makeText(context, "يرجى اختيار 'عدم التحسين' أو 'السماح بالعمل في الخلفية'", Toast.LENGTH_LONG).show()
                                    } catch (e: Exception) {
                                        try {
                                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (ex: Exception) {
                                            Toast.makeText(context, "افتح إعدادات الهاتف > البطارية > تحسين البطارية > اختر حاسبة الفليكسي", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurplePrimary),
                                modifier = Modifier.fillMaxWidth().testTag("disable_battery_optimization_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Power,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إيقاف توفير البطارية للتطبيق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 4: Password-Protected Manual Amount Adding
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) VibrantPurpleContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, if (isUnlocked) VibrantPurplePrimary else MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().testTag("manual_add_locked_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isUnlocked) VibrantPurplePrimary else VibrantFeeRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "إضافة مبلغ فليكسي يدوياً (كتابياً)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isUnlocked) "القسم مفتوح وجاهز للإدخال" else "مقفل بكلمة سر خاصة للمصادقة",
                                    fontSize = 10.sp,
                                    color = if (isUnlocked) VibrantPurplePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (!isUnlocked) {
                            // Password input field
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    passwordError = null
                                    if (it == REQUIRED_PASSWORD) {
                                        isUnlocked = true
                                        passwordError = null
                                    }
                                },
                                label = { Text("أدخل كلمة السر", fontSize = 12.sp) },
                                placeholder = { Text("كلمة السر المطلوبة", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "إظهار / إخفاء كلمة السر"
                                        )
                                    }
                                },
                                isError = passwordError != null,
                                modifier = Modifier.fillMaxWidth().testTag("password_input_field"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            if (passwordError != null) {
                                Text(
                                    text = passwordError ?: "",
                                    color = VibrantFeeRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = {
                                    if (passwordInput == REQUIRED_PASSWORD) {
                                        isUnlocked = true
                                        passwordError = null
                                    } else {
                                        passwordError = "كلمة المرور غير صحيحة!"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurplePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("unlock_manual_add_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فتح الإدخال اليدوي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            // Unlocked: Manual Input Form
                            OutlinedTextField(
                                value = manualAmountText,
                                onValueChange = {
                                    manualAmountText = it.filter { ch -> ch.isDigit() || ch == '.' }
                                    manualAddSuccessMsg = null
                                },
                                label = { Text("المبلغ بالدينار الجزائري (DA)", fontSize = 12.sp) },
                                placeholder = { Text("مثال: 1000 أو 500", fontSize = 11.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("manual_amount_input")
                            )

                            // Centimes live preview
                            val parsedAmount = manualAmountText.toDoubleOrNull()
                            if (parsedAmount != null && parsedAmount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = VibrantPurplePrimary.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "المعادل: ${CurrencyFormatter.getAlgerianSpokenLabel(parsedAmount)} ➔ الصافي: ${CurrencyFormatter.formatDinar(parsedAmount * (1.0 - selectedPercentage / 100.0))}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = VibrantPurplePrimary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = manualNoteText,
                                onValueChange = { manualNoteText = it },
                                label = { Text("ملاحظة (اختياري)", fontSize = 12.sp) },
                                placeholder = { Text("مثال: شحن يدوي للزبون", fontSize = 11.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("manual_note_input")
                            )

                            if (manualAddSuccessMsg != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = VibrantNetGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = manualAddSuccessMsg ?: "",
                                        color = VibrantNetGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val amt = manualAmountText.toDoubleOrNull()
                                    if (amt != null && amt > 0) {
                                        onAddManualTransaction(amt, manualNoteText.ifBlank { null })
                                        manualAddSuccessMsg = "تمت إضافة مبلغ $amt دج بنجاح!"
                                        manualAmountText = ""
                                        manualNoteText = ""
                                    }
                                },
                                enabled = (manualAmountText.toDoubleOrNull() ?: 0.0) > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPurplePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("submit_manual_amount_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("إضافة الرصيد وتسجيل العملية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Section 5: Mobilis SMS Senders Configuration
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
                                text = "أرقام ومرسلي فليكسي موبيليس",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = {
                                mobilisSenders = OperatorSenderConfig.DEFAULT_MOBILIS
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
                        text = "أرقام وأسماء مرسلي موبيليس (مفصولة بفاصلة) للتعرف على الرسائل تلقائياً:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    // Mobilis Card
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
                                placeholder = { Text("مثال: 644, 600, 606, Arseli, Mobilis, MOBILIS", fontSize = 11.sp) },
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
                                listOf("644", "600", "606", "Arseli", "Mobilis", "MOBILIS").forEach { tag ->
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
                            text = "مثال موبيليس: عند شحن 100 دج أو 1000 دج، يتم اقتطاع 10% تلقائياً وحساب الصافي بدقة.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Developer / Creator Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = VibrantPurpleContainer.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, VibrantPurplePrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("developer_info_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(VibrantPurplePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "صانع ومطور البرنامج:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ALILI ABD ELAZIZ AZA",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantPurplePrimary
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = VibrantPurplePrimary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "v2.1.0.2008",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantPurplePrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
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
                    onSaveOperatorSenders(mobilisSenders)
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
                Text("إغلاق")
            }
        }
    )
}
