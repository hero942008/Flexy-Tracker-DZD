package com.example.util

import com.example.data.model.OperatorSenderConfig
import java.util.regex.Pattern

data class ParsedFlexySms(
    val amount: Double,
    val operator: String,
    val sender: String,
    val rawBody: String
)

object FlexySmsParser {

    /**
     * Attempts to parse an incoming SMS as a Mobilis Flexy / mobile credit transfer message.
     * Supports configurable sender numbers for Mobilis (644, 600, 606, Arseli, Mobilis, MOBILIS, etc.)
     * and intelligent regex extraction for phrases like:
     * "Vous avez rechargé 100.00 DZD DA avec succès le 13/08/2026 15:52:46..."
     */
    fun parse(
        sender: String?,
        body: String?,
        config: OperatorSenderConfig = OperatorSenderConfig()
    ): ParsedFlexySms? {
        if (body.isNullOrBlank()) return null
        val cleanSender = (sender ?: "").trim()
        val normalizedBody = body.replace("\n", " ").trim()

        val operator = detectOperator(cleanSender, normalizedBody, config)
        val amount = extractAmount(normalizedBody) ?: return null

        // Validate amount (Flexy is usually >= 10 DA and <= 2,000,000 DA)
        if (amount <= 0 || amount > 2_000_000) return null

        return ParsedFlexySms(
            amount = amount,
            operator = operator,
            sender = cleanSender.ifBlank { operator },
            rawBody = body
        )
    }

    fun detectOperator(
        sender: String,
        body: String,
        config: OperatorSenderConfig = OperatorSenderConfig()
    ): String {
        val s = sender.lowercase().trim()
        val b = body.lowercase().trim()

        // 1. Check user-configured sender tokens first against sender address
        if (s.isNotEmpty()) {
            for (token in config.getMobilisTokens()) {
                val t = token.lowercase().trim()
                if (t.isNotEmpty() && (s == t || s.contains(t))) return "Mobilis"
            }
        }

        // 2. Check user-configured sender tokens against body keywords
        for (token in config.getMobilisTokens()) {
            val t = token.lowercase().trim()
            if (t.length >= 3 && b.contains(t)) return "Mobilis"
        }

        // 3. Fallback standard Mobilis keywords
        return "Mobilis"
    }

    private fun extractAmount(body: String): Double? {
        // Regex patterns tailored for Mobilis Flexy messages in Algeria:
        // Priority Pattern 1: "Vous avez rechargé 100.00 DZD DA avec succès" / "rechargé 100.00 DZD" / "100.00 DZD DA"
        val regexPatterns = listOf(
            // Target specific Mobilis format: "rechargé 100.00 DZD DA" / "rechargé 100.00 DZD" / "rechargé 1000 DA"
            Pattern.compile("(?i)(?:recharg[ée]|rechargement|recharge|solde|cr[ée]dit|re[çc]u|montant|valeur|somme|vers[ée])\\s*(?:de|d'un montant de|:)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DZD\\s*DA|DZD|DA|دج)", Pattern.CASE_INSENSITIVE),
            
            // Arabic format: "تم شحن / تعبئة رصيدك بمبلغ 1000 دج"
            Pattern.compile("(?:شحن|تعبئة|استلام|رصيد|مبلغ|تم تحويل|تمت تعبئة)\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:دج|دينار|DA|DZD)", Pattern.CASE_INSENSITIVE),
            
            // Number followed by DZD DA or DZD or DA
            Pattern.compile("([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DZD\\s*DA|DZD|DA|دج)", Pattern.CASE_INSENSITIVE),
            
            // Fallback: Number before/after DA
            Pattern.compile("(?i)DA\\s*([0-9]+(?:[.,][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in regexPatterns) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val matchGroup = matcher.group(1)?.replace(",", ".")?.trim()
                val parsed = matchGroup?.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    return parsed
                }
            }
        }

        // Generic fallback: find any standalone number that fits typical flexy recharge amounts
        val fallbackPattern = Pattern.compile("\\b([0-9]{2,7}(?:\\.[0-9]{1,2})?)\\b")
        val fallbackMatcher = fallbackPattern.matcher(body)
        while (fallbackMatcher.find()) {
            val candidate = fallbackMatcher.group(1)?.toDoubleOrNull()
            if (candidate != null && candidate >= 50 && candidate <= 500000) {
                // If it looks like a year/date like 2024, 2025, 2026 skip if it's near slashes
                val matchStart = fallbackMatcher.start()
                val matchEnd = fallbackMatcher.end()
                val beforeChar = if (matchStart > 0) body[matchStart - 1] else ' '
                val afterChar = if (matchEnd < body.length) body[matchEnd] else ' '
                if (beforeChar != '/' && beforeChar != '-' && afterChar != '/' && afterChar != '-') {
                    return candidate
                }
            }
        }

        return null
    }
}
