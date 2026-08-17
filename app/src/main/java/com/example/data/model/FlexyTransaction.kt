package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flexy_transactions")
data class FlexyTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,              // Gross amount in Dinar (DA) e.g., 1000.0 DA (= 100 alf)
    val cutPercentage: Double = 10.0,// Percentage cut, default 10%
    val cutAmount: Double,           // Calculated cut amount (amount * cutPercentage / 100.0)
    val netAmount: Double,           // Net after cut (amount - cutAmount)
    val operatorName: String,        // "Mobilis", "Djezzy", "Ooredoo", or "يدوي"
    val senderNumber: String? = null,// e.g. "644", "710", "555" or phone number
    val rawMessage: String? = null,  // Original SMS body if auto-detected
    val timestamp: Long = System.currentTimeMillis(),
    val isAutoDetected: Boolean = false,
    val note: String? = null
)
