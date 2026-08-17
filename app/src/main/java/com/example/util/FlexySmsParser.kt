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
     * Attempts to parse an incoming SMS as an Algerian Flexy / mobile credit transfer message.
     * Supports configurable sender numbers for Mobilis, Djezzy, and Ooredoo.
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
            for (token in config.getDjezzyTokens()) {
                val t = token.lowercase().trim()
                if (t.isNotEmpty() && (s == t || s.contains(t))) return "Djezzy"
            }
            for (token in config.getOoredooTokens()) {
                val t = token.lowercase().trim()
                if (t.isNotEmpty() && (s == t || s.contains(t))) return "Ooredoo"
            }
        }

        // 2. Check user-configured sender tokens against body keywords
        for (token in config.getMobilisTokens()) {
            val t = token.lowercase().trim()
            if (t.length >= 3 && b.contains(t)) return "Mobilis"
        }
        for (token in config.getDjezzyTokens()) {
            val t = token.lowercase().trim()
            if (t.length >= 3 && b.contains(t)) return "Djezzy"
        }
        for (token in config.getOoredooTokens()) {
            val t = token.lowercase().trim()
            if (t.length >= 3 && b.contains(t)) return "Ooredoo"
        }

        // 3. Fallback standard Algerian operator keywords
        return when {
            s.contains("mobilis") || s == "644" || s == "600" || b.contains("mobilis") || b.contains("arseli") || b.contains("أرسلي") || b.contains("موبيليس") -> "Mobilis"
            s.contains("djezzy") || s == "710" || s == "700" || b.contains("djezzy") || b.contains("flexy") || b.contains("فليكسي") || b.contains("جيزي") -> "Djezzy"
            s.contains("ooredoo") || s == "555" || s == "500" || b.contains("ooredoo") || b.contains("storm") || b.contains("maxy") || b.contains("نجمة") || b.contains("اوريدو") || b.contains("أوريدو") -> "Ooredoo"
            else -> "فليكسي"
        }
    }

    private fun extractAmount(body: String): Double? {
        // Common regex patterns for Flexy messages in Algeria:
        // Pattern 1: "reçu ... 1000 DA" / "rechargement de 1000.00 DA" / "1000 DA"
        val regexPatterns = listOf(
            // "de 1000 DA" or "1000.00 DA" or "1000 DA" or "1000 DZD"
            Pattern.compile("(?i)(?:rechargement|recharge|solde|cr[ée]dit|re[çc]u|montant|valeur|somme|vers[ée])\\s*(?:de|d'un montant de|:)?\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DA|دج|DZD)", Pattern.CASE_INSENSITIVE),
            // Arabic format: "تم شحن / تعبئة رصيدك بمبلغ 1000 دج"
            Pattern.compile("(?:شحن|تعبئة|استلام|رصيد|مبلغ|تم تحويل)\\s*([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:دج|دينار|DA|DZD)", Pattern.CASE_INSENSITIVE),
            // Fallback: Number followed immediately or closely by DA / دج / DZD
            Pattern.compile("([0-9]+(?:[.,][0-9]{1,2})?)\\s*(?:DA|دج|DZD)", Pattern.CASE_INSENSITIVE),
            // Number followed by DA in parenthesis or prefix
            Pattern.compile("(?i)DA\\s*([0-9]+(?:[.,][0-9]{1,2})?)", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in regexPatterns) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val match = matcher.group(1)?.replace(",", ".")?.trim()
                val parsed = match?.toDoubleOrNull()
                if (parsed != null && parsed > 0) {
                    return parsed
                }
            }
        }

        return null
    }

    /**
     * Provide ready sample SMS texts to easily test the app in emulator or on device.
     */
    val SAMPLE_TEST_MESSAGES = listOf(
        Pair("Mobilis (644)", "Arseli: Vous avez reçu un rechargement de 1000 DA de 0661234567. Votre nouveau solde est 1500.00 DA."),
        Pair("Djezzy (710)", "Djezzy Flexy: Rechargement de 2000 DA effectué avec succès. Merci de votre fidélité."),
        Pair("Ooredoo (555)", "Storm Ooredoo: Vous avez reçu 500 DA. Nouveau solde: 750 DA."),
        Pair("موبيليس (بالعربية)", "أرسلي: تم تعبئة رصيدك بمبلغ 1000 دج بنجاح."),
        Pair("جيزي (بالعربية)", "فليكسي جيزي: تم استلام مبلغ 2000 دج في حسابك.")
    )
}
