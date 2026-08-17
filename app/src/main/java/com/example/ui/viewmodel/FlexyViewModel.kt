package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.FlexyTransaction
import com.example.data.model.OperatorSenderConfig
import com.example.data.repository.CurrencyMode
import com.example.data.repository.DateFilter
import com.example.data.repository.FlexyRepository
import com.example.util.CurrencyFormatter
import com.example.util.FlexySmsParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FlexySummary(
    val totalGross: Double = 0.0,
    val totalCut: Double = 0.0,
    val totalNet: Double = 0.0,
    val count: Int = 0
)

data class FlexyUiState(
    val transactions: List<FlexyTransaction> = emptyList(),
    val filteredTransactions: List<FlexyTransaction> = emptyList(),
    val summary: FlexySummary = FlexySummary(),
    val selectedFilter: DateFilter = DateFilter.TODAY,
    val currencyMode: CurrencyMode = CurrencyMode.DINAR,
    val cutPercentage: Double = 10.0,
    val operatorSenderConfig: OperatorSenderConfig = OperatorSenderConfig(),
    val isSmsPermissionGranted: Boolean = false,
    val isAddDialogOpen: Boolean = false,
    val isSmsTestDialogOpen: Boolean = false,
    val isSettingsDialogOpen: Boolean = false
)

class FlexyViewModel(
    private val repository: FlexyRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(DateFilter.TODAY)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _isSmsPermissionGranted = MutableStateFlow(false)
    val isSmsPermissionGranted = _isSmsPermissionGranted.asStateFlow()

    private val _isAddDialogOpen = MutableStateFlow(false)
    val isAddDialogOpen = _isAddDialogOpen.asStateFlow()

    private val _isSmsTestDialogOpen = MutableStateFlow(false)
    val isSmsTestDialogOpen = _isSmsTestDialogOpen.asStateFlow()

    private val _isSettingsDialogOpen = MutableStateFlow(false)
    val isSettingsDialogOpen = _isSettingsDialogOpen.asStateFlow()

    // Combine data flows cleanly
    private val transactionsAndFilter = combine(
        repository.allTransactions,
        _selectedFilter
    ) { items, filter ->
        val (start, end) = FlexyRepository.getDateRange(filter)
        val filtered = items.filter { it.timestamp in start..end }
        val totalGross = filtered.sumOf { it.amount }
        val totalCut = filtered.sumOf { it.cutAmount }
        val totalNet = filtered.sumOf { it.netAmount }

        Triple(items, filtered, FlexySummary(totalGross, totalCut, totalNet, filtered.size))
    }

    private val appSettings = combine(
        repository.currencyMode,
        repository.defaultPercentage,
        repository.operatorSenders
    ) { currency, percent, sendersConfig ->
        Triple(currency, percent, sendersConfig)
    }

    private val dialogsState = combine(
        _isSmsPermissionGranted,
        _isAddDialogOpen,
        _isSmsTestDialogOpen,
        _isSettingsDialogOpen
    ) { smsPerm, addOpen, smsTestOpen, settingsOpen ->
        listOf(smsPerm, addOpen, smsTestOpen, settingsOpen)
    }

    val uiState: StateFlow<FlexyUiState> = combine(
        transactionsAndFilter,
        appSettings,
        _selectedFilter,
        dialogsState
    ) { (items, filtered, summary), (currency, percent, sendersConfig), filter, dialogs ->
        FlexyUiState(
            transactions = items,
            filteredTransactions = filtered,
            summary = summary,
            selectedFilter = filter,
            currencyMode = currency,
            cutPercentage = percent,
            operatorSenderConfig = sendersConfig,
            isSmsPermissionGranted = dialogs[0],
            isAddDialogOpen = dialogs[1],
            isSmsTestDialogOpen = dialogs[2],
            isSettingsDialogOpen = dialogs[3]
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FlexyUiState()
    )

    fun setFilter(filter: DateFilter) {
        _selectedFilter.value = filter
    }

    fun setCurrencyMode(mode: CurrencyMode) {
        repository.setCurrencyMode(mode)
    }

    fun setCutPercentage(percent: Double) {
        repository.setDefaultPercentage(percent)
    }

    fun saveOperatorSenders(mobilis: String, djezzy: String, ooredoo: String) {
        repository.setOperatorSenders(
            OperatorSenderConfig(
                mobilisSenders = mobilis.trim(),
                djezzySenders = djezzy.trim(),
                ooredooSenders = ooredoo.trim()
            )
        )
    }

    fun resetOperatorSenders() {
        repository.resetOperatorSenders()
    }

    fun setSmsPermissionGranted(granted: Boolean) {
        _isSmsPermissionGranted.value = granted
    }

    fun setAddDialogOpen(open: Boolean) {
        _isAddDialogOpen.value = open
    }

    fun setSmsTestDialogOpen(open: Boolean) {
        _isSmsTestDialogOpen.value = open
    }

    fun setSettingsDialogOpen(open: Boolean) {
        _isSettingsDialogOpen.value = open
    }

    fun addManualTransaction(
        amount: Double,
        operator: String = "يدوي",
        percentage: Double = uiState.value.cutPercentage,
        note: String? = null
    ) {
        if (amount <= 0) return
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                cutPercentage = percentage,
                operatorName = operator,
                isAutoDetected = false,
                note = note
            )
            _isAddDialogOpen.value = false
        }
    }

    fun simulateSmsReceived(sender: String, body: String) {
        val parsed = FlexySmsParser.parse(sender, body, uiState.value.operatorSenderConfig) ?: return
        viewModelScope.launch {
            repository.addTransaction(
                amount = parsed.amount,
                cutPercentage = uiState.value.cutPercentage,
                operatorName = parsed.operator,
                senderNumber = parsed.sender,
                rawMessage = parsed.rawBody,
                isAutoDetected = true
            )
            _isSmsTestDialogOpen.value = false
        }
    }

    fun deleteTransaction(transaction: FlexyTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun getShareableSummaryText(): String {
        val state = uiState.value
        val summary = state.summary
        val filterName = state.selectedFilter.titleAr
        val percentageStr = "${state.cutPercentage.toInt()}%"

        val grossDinar = CurrencyFormatter.formatDinar(summary.totalGross)
        val grossCentimes = CurrencyFormatter.getAlgerianSpokenLabel(summary.totalGross)
        val cutDinar = CurrencyFormatter.formatDinar(summary.totalCut)
        val netDinar = CurrencyFormatter.formatDinar(summary.totalNet)
        val netCentimes = CurrencyFormatter.getAlgerianSpokenLabel(summary.totalNet)

        return """
📊 *تقرير حساب الفليكسي ($filterName)*:
━━━━━━━━━━━━━━━━━
💰 *المدخول الإجمالي*: $grossDinar
🏷️ *بالسنتيم*: $grossCentimes
✂️ *النسبة المقتطعة ($percentageStr)*: $cutDinar
━━━━━━━━━━━━━━━━━
✅ *الصافي النهائي*: $netDinar
✨ *بالسنتيم*: $netCentimes
📝 *عدد العمليات*: ${summary.count} عملية
━━━━━━━━━━━━━━━━━
📱 تم الإنشاء بواسطة تطبيق حاسبة الفليكسي
        """.trimIndent()
    }

    fun shareSummary(context: Context) {
        val text = getShareableSummaryText()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة تقرير الفليكسي والصافي")
        context.startActivity(shareIntent)
    }

    class Factory(private val repository: FlexyRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FlexyViewModel::class.java)) {
                return FlexyViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
