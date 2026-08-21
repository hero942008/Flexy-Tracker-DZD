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
     * Strict parser specifically designed for Mobilis Flexy messages:
     * Examples:
     * "Vous avez rechargé 100.00 DZD DA avec succès le 21/08/2026 10:38:46. Profitez d’une multitude d’avantages avec les nouvelles offres MOBILIS."
     * "Vous avez rechargé 500 DZD DA avec succès le 21/08/2026..."
     * "Arseli: Vous avez reçu un rechargement de 1000 DA"
     * 
     * Strictly verifies that the SMS is from Mobilis only and strictly matches a genuine Flexy recharge.
     */
    fun parse(
        sender: String?,
        body: String?,
        config: OperatorSenderConfig = OperatorSenderConfig()
    ): ParsedFlexySms? {
        if (body.isNullOrBlank()) return null
        val cleanSender = (sender ?: "").trim()
        val normalizedBody = body.replace("\n", " ").trim()

        // 1. Strict Mobilis origin & validity check
        if (!isMobilisSender(cleanSender, normalizedBody, config)) {
            return null
        }

        // 2. Extract recharge amount strictly from Mobilis format
        val amount = extractAmount(normalizedBody) ?: return null

        // Validate amount (Flexy amount between 10 DA and 2,000,000 DA)
        if (amount <= 0 || amount > 2_000_000) return null

        return ParsedFlexySms(
            amount = amount,
            operator = "Mobilis",
            sender = cleanSender.ifBlank { "Mobilis" },
            rawBody = body
        )
    }

    /**
     * Strictly verifies that the message is from Mobilis.
     * Rejects any normal phone number or non-Mobilis sender.
     */
    fun isMobilisSender(
        sender: String,
        body: String,
        config: OperatorSenderConfig = OperatorSenderConfig()
    ): Boolean {
        val s = sender.lowercase().trim()
        val b = body.lowercase().trim()

        val mobilisTokens = config.getMobilisTokens().map { it.lowercase().trim() }.filter { it.isNotEmpty() }

        // Reject standard personal phone numbers unless explicitly added by the user in Mobilis sender tokens
        val isPersonalPhoneNumber = s.matches(Regex("^(?:\\+?213|00213|0)[567][0-9]{8}$"))
        val isConfiguredToken = mobilisTokens.any { it == s }

        if (isPersonalPhoneNumber && !isConfiguredToken) {
            return false
        }

        // 1. If sender matches known Mobilis sender identifiers (644, 600, 606, Arseli, Mobilis, etc.)
        if (s.isNotEmpty()) {
            for (token in mobilisTokens) {
                if (s == token) return true
                if (token.all { it.isDigit() } && s.contains(token) && s.length <= 8) return true
                if (token.length >= 3 && s.contains(token)) return true
            }
        }

        // 2. If sender is alphanumeric or empty, require the SMS body to explicitly contain Mobilis / Arseli AND recharge keywords
        val hasMobilisBrand = b.contains("mobilis") || b.contains("arseli") || b.contains("موبيليس") || b.contains("أرسلي") || b.contains("644")
        val hasRechargeIndicator = b.contains("recharg") || b.contains("vous avez rechargé") || b.contains("تعبئة") || b.contains("شحن")

        return hasMobilisBrand && hasRechargeIndicator
    }

    private fun extractAmount(body: String): Double? {
        // Regex strictly matching Mobilis Flexy formats:
        // Format 1: "Vous avez rechargé 100.00 DZD DA avec succès..." / "Vous avez rechargé 1000 DZD DA..."
        val primaryMobilisRegex = Pattern.compile(
            "(?i)Vous\\s+avez\\s+recharg[ée]\\s+([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DZD\\s*DA|DZD|DA)\\s+avec\\s+succ[èe]s",
            Pattern.CASE_INSENSITIVE
        )
        val matcher1 = primaryMobilisRegex.matcher(body)
        if (matcher1.find()) {
            val amountStr = matcher1.group(1)?.replace(",", ".")?.trim()
            val parsed = amountStr?.toDoubleOrNull()
            if (parsed != null && parsed > 0) return parsed
        }

        // Format 2: "rechargé 100.00 DZD DA" / "rechargement de 1000 DA" / "recharge 500 DA"
        val generalRechargeRegex = Pattern.compile(
            "(?i)(?:recharg[ée]|rechargement|recharge|solde|cr[ée]dit|re[çc]u)\\s*(?:de|d'un montant de|:)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DZD\\s*DA|DZD|DA)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher2 = generalRechargeRegex.matcher(body)
        if (matcher2.find()) {
            val amountStr = matcher2.group(1)?.replace(",", ".")?.trim()
            val parsed = amountStr?.toDoubleOrNull()
            if (parsed != null && parsed > 0) return parsed
        }

        // Format 3: Arabic format "تمت تعبئة / شحن رصيدك بمبلغ 500 دج"
        val arabicRechargeRegex = Pattern.compile(
            "(?:شحن|تعبئة|استلام|رصيد|مبلغ|تمت تعبئة|تم شحن)\\s*(?:رصيدك)?\\s*(?:بمبلغ)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:دج|دينار|DA|DZD)",
            Pattern.CASE_INSENSITIVE
        )
        val matcher3 = arabicRechargeRegex.matcher(body)
        if (matcher3.find()) {
            val amountStr = matcher3.group(1)?.replace(",", ".")?.trim()
            val parsed = amountStr?.toDoubleOrNull()
            if (parsed != null && parsed > 0) return parsed
        }

        return null
    }
}

