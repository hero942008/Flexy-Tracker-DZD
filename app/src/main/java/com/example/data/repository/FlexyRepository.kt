package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.local.FlexyDao
import com.example.data.model.FlexyTransaction
import com.example.data.model.OperatorSenderConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

enum class CurrencyMode {
    DINAR,     // دج (DA)
    CENTIMES   // سنتيم (Centimes: 1000 DA = 100 ألف سنتيم)
}

enum class DateFilter(val titleAr: String) {
    TODAY("اليوم"),
    YESTERDAY("أمس"),
    THIS_WEEK("هذا الأسبوع"),
    THIS_MONTH("هذا الشهر"),
    ALL("الكل")
}

class FlexyRepository(
    private val flexyDao: FlexyDao,
    context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("flexy_tracker_prefs", Context.MODE_PRIVATE)

    private val _defaultPercentage = MutableStateFlow(
        prefs.getFloat("default_cut_percentage", 10.0f).toDouble()
    )
    val defaultPercentage: StateFlow<Double> = _defaultPercentage.asStateFlow()

    private val _currencyMode = MutableStateFlow(
        try {
            CurrencyMode.valueOf(prefs.getString("currency_mode", CurrencyMode.DINAR.name) ?: CurrencyMode.DINAR.name)
        } catch (e: Exception) {
            CurrencyMode.DINAR
        }
    )
    val currencyMode: StateFlow<CurrencyMode> = _currencyMode.asStateFlow()

    private val _operatorSenders = MutableStateFlow(
        OperatorSenderConfig(
            mobilisSenders = prefs.getString("senders_mobilis", OperatorSenderConfig.DEFAULT_MOBILIS) ?: OperatorSenderConfig.DEFAULT_MOBILIS
        )
    )
    val operatorSenders: StateFlow<OperatorSenderConfig> = _operatorSenders.asStateFlow()

    val allTransactions: Flow<List<FlexyTransaction>> = flexyDao.getAllTransactions()

    suspend fun addTransaction(
        amount: Double,
        cutPercentage: Double = _defaultPercentage.value,
        operatorName: String = "يدوي",
        senderNumber: String? = null,
        rawMessage: String? = null,
        isAutoDetected: Boolean = false,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis()
    ): Long {
        val cut = (amount * cutPercentage) / 100.0
        val net = amount - cut
        val transaction = FlexyTransaction(
            amount = amount,
            cutPercentage = cutPercentage,
            cutAmount = cut,
            netAmount = net,
            operatorName = operatorName,
            senderNumber = senderNumber,
            rawMessage = rawMessage,
            isAutoDetected = isAutoDetected,
            note = note,
            timestamp = timestamp
        )
        return flexyDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: FlexyTransaction) {
        flexyDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        flexyDao.deleteById(id)
    }

    suspend fun clearAll() {
        flexyDao.clearAll()
    }

    fun setDefaultPercentage(percent: Double) {
        _defaultPercentage.value = percent
        prefs.edit().putFloat("default_cut_percentage", percent.toFloat()).apply()
    }

    fun setCurrencyMode(mode: CurrencyMode) {
        _currencyMode.value = mode
        prefs.edit().putString("currency_mode", mode.name).apply()
    }

    fun getOperatorSenderConfig(): OperatorSenderConfig {
        return _operatorSenders.value
    }

    fun setOperatorSenders(config: OperatorSenderConfig) {
        _operatorSenders.value = config
        prefs.edit()
            .putString("senders_mobilis", config.mobilisSenders)
            .apply()
    }

    fun resetOperatorSenders() {
        val defaultConfig = OperatorSenderConfig()
        setOperatorSenders(defaultConfig)
    }

    companion object {
        fun getDateRange(filter: DateFilter): Pair<Long, Long> {
            val calendar = Calendar.getInstance()
            val now = System.currentTimeMillis()

            return when (filter) {
                DateFilter.TODAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    Pair(start, Long.MAX_VALUE)
                }
                DateFilter.YESTERDAY -> {
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val todayStart = calendar.timeInMillis
                    calendar.add(Calendar.DAY_OF_YEAR, -1)
                    val yesterdayStart = calendar.timeInMillis
                    Pair(yesterdayStart, todayStart - 1)
                }
                DateFilter.THIS_WEEK -> {
                    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    Pair(start, Long.MAX_VALUE)
                }
                DateFilter.THIS_MONTH -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)
                    val start = calendar.timeInMillis
                    Pair(start, Long.MAX_VALUE)
                }
                DateFilter.ALL -> Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
