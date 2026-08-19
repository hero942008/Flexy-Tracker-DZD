package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.CurrencyMode
import com.example.data.repository.DateFilter
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.QuickAddBar
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SmsTestDialog
import com.example.ui.components.SummaryCards
import com.example.ui.components.TransactionItem
import com.example.ui.theme.OnVibrantBlueFab
import com.example.ui.theme.OnVibrantPurpleContainer
import com.example.ui.theme.VibrantBlueFab
import com.example.ui.theme.VibrantFeeRed
import com.example.ui.theme.VibrantNetGreen
import com.example.ui.theme.VibrantPurpleContainer
import com.example.ui.theme.VibrantPurplePrimary
import com.example.ui.viewmodel.FlexyViewModel

@Composable
fun HomeScreen(
    viewModel: FlexyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Permission launcher for SMS reception and reading
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val receiveGranted = permissions[Manifest.permission.RECEIVE_SMS] == true
        val readGranted = permissions[Manifest.permission.READ_SMS] == true
        viewModel.setSmsPermissionGranted(receiveGranted || readGranted)
    }

    LaunchedEffect(Unit) {
        val hasReceive = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val hasRead = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.setSmsPermissionGranted(hasReceive || hasRead)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
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
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(VibrantPurplePrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "حاسبة الفليكسي",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "حساب واقتطاع الصافي تلقائياً",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Top Actions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // SMS Test button
                            IconButton(
                                onClick = { viewModel.setSmsTestDialogOpen(true) },
                                modifier = Modifier.testTag("sms_test_top_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = "تجربة رسالة فليكسي",
                                    tint = VibrantPurplePrimary
                                )
                            }

                            // Share Summary Button
                            IconButton(
                                onClick = { viewModel.shareSummary(context) },
                                modifier = Modifier.testTag("share_summary_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "مشاركة التقرير",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Settings Button
                            IconButton(
                                onClick = { viewModel.setSettingsDialogOpen(true) },
                                modifier = Modifier.testTag("settings_top_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "الإعدادات",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SMS Listener Status / Request Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (uiState.isSmsPermissionGranted) VibrantPurpleContainer.copy(alpha = 0.6f) else VibrantFeeRed.copy(alpha = 0.1f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!uiState.isSmsPermissionGranted) {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.RECEIVE_SMS,
                                            Manifest.permission.READ_SMS
                                        )
                                    )
                                }
                            }
                            .testTag("sms_permission_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isSmsPermissionGranted) Icons.Default.Check else Icons.Default.PhoneAndroid,
                                    contentDescription = null,
                                    tint = if (uiState.isSmsPermissionGranted) VibrantPurplePrimary else VibrantFeeRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (uiState.isSmsPermissionGranted) "قراءة رسائل الفليكسي التلقائية (موبيليس، جيزي، أوريدو) مفعّلة" else "انقر هنا لتفعيل قراءة رسائل الفليكسي التلقائية",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.isSmsPermissionGranted) OnVibrantPurpleContainer else VibrantFeeRed
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.setAddDialogOpen(true) },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = "تسجيل فليكسي",
                        fontWeight = FontWeight.Bold
                    )
                },
                containerColor = VibrantBlueFab,
                contentColor = OnVibrantBlueFab,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("fab_add_flexy")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Date Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DateFilter.values().forEach { filter ->
                        val isSelected = uiState.selectedFilter == filter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setFilter(filter) },
                            label = {
                                Text(
                                    text = filter.titleAr,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VibrantPurplePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) VibrantPurplePrimary else MaterialTheme.colorScheme.outline
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("filter_chip_${filter.name}")
                        )
                    }
                }
            }

            // 2. Summary Cards (Net Hero, Gross, Deducted 10%)
            item {
                SummaryCards(
                    summary = uiState.summary,
                    currencyMode = uiState.currencyMode,
                    cutPercentage = uiState.cutPercentage,
                    filterName = uiState.selectedFilter.titleAr,
                    onToggleCurrency = {
                        val newMode = if (uiState.currencyMode == CurrencyMode.DINAR) CurrencyMode.CENTIMES else CurrencyMode.DINAR
                        viewModel.setCurrencyMode(newMode)
                    },
                    onOpenSettings = { viewModel.setSettingsDialogOpen(true) }
                )
            }

            // 3. Quick Add Bar
            item {
                QuickAddBar(
                    onQuickAdd = { amount ->
                        viewModel.addManualTransaction(amount)
                    },
                    onCustomAdd = { viewModel.setAddDialogOpen(true) }
                )
            }

            // 4. Transactions List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سجل العمليات (${uiState.filteredTransactions.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (uiState.filteredTransactions.isNotEmpty()) {
                        Text(
                            text = "مرتبة حسب الأحدث",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 5. Transactions List or Empty State
            if (uiState.filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("empty_transactions_state"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(VibrantPurpleContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inbox,
                                    contentDescription = null,
                                    tint = OnVibrantPurpleContainer,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Text(
                                text = "لا توجد عمليات فليكسي مسجلة لـ ${uiState.selectedFilter.titleAr}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "استخدم أزرار الإضافة السريعة أعلاه أو جرب محاكاة رسالة SMS",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        currencyMode = uiState.currencyMode,
                        onDelete = { viewModel.deleteTransaction(it) }
                    )
                }
            }

            // Bottom Spacer for FAB clearance
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Dialogs
    if (uiState.isAddDialogOpen) {
        AddTransactionDialog(
            defaultPercentage = uiState.cutPercentage,
            currencyMode = uiState.currencyMode,
            onDismiss = { viewModel.setAddDialogOpen(false) },
            onConfirm = { amount, operator, percentage, note ->
                viewModel.addManualTransaction(amount, operator, percentage, note)
            }
        )
    }

    if (uiState.isSmsTestDialogOpen) {
        SmsTestDialog(
            onDismiss = { viewModel.setSmsTestDialogOpen(false) },
            onSimulate = { sender, body ->
                viewModel.simulateSmsReceived(sender, body)
            }
        )
    }

    if (uiState.isSettingsDialogOpen) {
        SettingsDialog(
            currentPercentage = uiState.cutPercentage,
            currencyMode = uiState.currencyMode,
            operatorSenders = uiState.operatorSenderConfig,
            onDismiss = { viewModel.setSettingsDialogOpen(false) },
            onSavePercentage = { viewModel.setCutPercentage(it) },
            onSaveOperatorSenders = { mob, dj, oor ->
                viewModel.saveOperatorSenders(mob, dj, oor)
            },
            onResetOperatorSenders = {
                viewModel.resetOperatorSenders()
            },
            onToggleCurrency = { viewModel.setCurrencyMode(it) },
            onClearAll = { viewModel.clearAll() }
        )
    }
}

