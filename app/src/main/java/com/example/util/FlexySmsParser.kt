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
     * Strictly verifies that the message originates from Mobilis (644, 600, 606, Arseli, Mobilis, etc.)
     * or contains explicit Mobilis Flexy recharge context with currency units (DA / DZD / دج).
     */
    fun parse(
        sender: String?,
        body: String?,
        config: OperatorSenderConfig = OperatorSenderConfig()
    ): ParsedFlexySms? {
        if (body.isNullOrBlank()) return null
        val cleanSender = (sender ?: "").trim()
        val normalizedBody = body.replace("\n", " ").trim()

        // 1. Strict Mobilis origin / context check
        if (!isMobilisSenderOrContext(cleanSender, normalizedBody, config)) {
            return null
        }

        // 2. Extract amount strictly tied to recharge context & currency (DA / DZD / دج)
        val amount = extractAmount(normalizedBody) ?: return null

        // Validate amount (Mobilis Flexy is usually >= 10 DA and <= 2,000,000 DA)
        if (amount <= 0 || amount > 2_000_000) return null

        return ParsedFlexySms(
            amount = amount,
            operator = "Mobilis",
            sender = cleanSender.ifBlank { "Mobilis" },
            rawBody = body
        )
    }

    /**
     * Checks if the sender or message context strictly matches Mobilis.
     * Rejects regular phone numbers and arbitrary contacts (e.g. personal SMS).
     */
    fun isMobilisSenderOrContext(
        sender: String,
        body: String,
        config: OperatorSenderConfig = OperatorSenderConfig()
    ): Boolean {
        val s = sender.lowercase().trim()
        val b = body.lowercase().trim()

        val mobilisTokens = config.getMobilisTokens().map { it.lowercase().trim() }.filter { it.isNotEmpty() }

        // 1. Exact or prefix/suffix match on configured Mobilis sender IDs (644, 600, 606, Arseli, Mobilis, etc.)
        if (s.isNotEmpty()) {
            for (token in mobilisTokens) {
                if (s == token || s.equals(token, ignoreCase = true)) {
                    return true
                }
                // Short codes like 644, 600, 606
                if (token.all { it.isDigit() } && s.contains(token) && s.length <= 8) {
                    return true
                }
                // Name tokens like mobilis, arseli
                if (token.length >= 3 && s.contains(token)) {
                    return true
                }
            }
        }

        // 2. If sender is a personal phone number (e.g. starts with 05, 06, 07, +213, 00213) and not in allowed tokens, reject
        val isPersonalPhoneNumber = s.matches(Regex("^(?:\\+?213|00213|0)[567][0-9]{8}$"))
        if (isPersonalPhoneNumber) {
            return false
        }

        // 3. If sender is unknown/empty or an alphanumeric shortcode, require explicit Mobilis / Arseli keywords in the body
        val hasMobilisKeywordInBody = b.contains("mobilis") || 
                b.contains("arseli") || 
                b.contains("أرسلي") || 
                b.contains("موبيليس") ||
                b.contains("644")

        val hasRechargeKeywordInBody = b.contains("recharg") || 
                b.contains("crédit") || 
                b.contains("credit") || 
                b.contains("solde") || 
                b.contains("تعبئة") || 
                b.contains("شحن")

        return hasMobilisKeywordInBody && hasRechargeKeywordInBody
    }

    private fun extractAmount(body: String): Double? {
        // Regex patterns tailored for Mobilis Flexy messages in Algeria:
        val regexPatterns = listOf(
            // Target specific Mobilis format: "rechargé 100.00 DZD DA" / "rechargé 100.00 DZD" / "rechargé 1000 DA"
            Pattern.compile("(?i)(?:recharg[ée]|rechargement|recharge|solde|cr[ée]dit|re[çc]u|montant|valeur|somme|vers[ée])\\s*(?:de|d'un montant de|:)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DZD\\s*DA|DZD|DA|دج|دينار)", Pattern.CASE_INSENSITIVE),
            
            // Arabic format: "تم شحن / تعبئة رصيدك بمبلغ 1000 دج"
            Pattern.compile("(?:شحن|تعبئة|استلام|رصيد|مبلغ|تم تحويل|تمت تعبئة|تم شحن)\\s*(?:رصيدك)?\\s*(?:بمبلغ)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:دج|دينار|DA|DZD)", Pattern.CASE_INSENSITIVE),
            
            // Number immediately followed by DZD DA, DZD, DA, or دج in a message with recharge context
            Pattern.compile("(?i)\\b([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DZD\\s*DA|DZD|DA|دج|دينار)\\b", Pattern.CASE_INSENSITIVE)
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

        // Note: No generic fallback pattern on plain numbers to prevent false positives from normal messages
        return null
    }
}
