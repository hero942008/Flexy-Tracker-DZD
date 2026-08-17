package com.example.util

import com.example.data.repository.CurrencyMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyFormatter {

    private val numberFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
            decimalSeparator = '.'
        }
        DecimalFormat("#,##0.##", symbols)
    }

    private val integerFormat: DecimalFormat by lazy {
        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
        }
        DecimalFormat("#,##0", symbols)
    }

    /**
     * Formats an amount in DA (Dinars)
     */
    fun formatDinar(amount: Double): String {
        return "${numberFormat.format(amount)} دج"
    }

    /**
     * Formats an amount in Centimes (سنتيم)
     * 1 DA = 100 Centimes
     */
    fun formatCentimes(amountInDinar: Double): String {
        val centimes = amountInDinar * 100.0
        return "${integerFormat.format(centimes)} سنتيم"
    }

    /**
     * Returns Algerian colloquial descriptor e.g. "100 ألف سنتيم" or "مليون سنتيم"
     */
    fun getAlgerianSpokenLabel(amountInDinar: Double): String {
        val centimes = amountInDinar * 100.0
        return when {
            centimes >= 10_000_000 -> {
                val millions = centimes / 1_000_000.0
                "${numberFormat.format(millions)} ملايين سنتيم"
            }
            centimes >= 1_000_000 -> {
                val millions = centimes / 1_000_000.0
                "${numberFormat.format(millions)} مليون سنتيم"
            }
            centimes >= 1_000 -> {
                val thousands = centimes / 1_000.0
                "${numberFormat.format(thousands)} ألف سنتيم"
            }
            else -> "${integerFormat.format(centimes)} سنتيم"
        }
    }

    /**
     * Formats depending on selected mode
     */
    fun formatAmount(amountInDinar: Double, mode: CurrencyMode): String {
        return when (mode) {
            CurrencyMode.DINAR -> formatDinar(amountInDinar)
            CurrencyMode.CENTIMES -> formatCentimes(amountInDinar)
        }
    }

    /**
     * Formats timestamp into Arabic/French friendly format
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd  •  HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("ar"))
        return sdf.format(Date(timestamp))
    }
}
