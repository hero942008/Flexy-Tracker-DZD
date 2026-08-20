package com.example.data.model

/**
 * Configuration for Mobilis SMS senders.
 * Users can customize the phone numbers / sender names (e.g. 644, 600, 606, Arseli, Mobilis, MOBILIS)
 * that Mobilis uses to deliver Flexy notifications.
 */
data class OperatorSenderConfig(
    val mobilisSenders: String = DEFAULT_MOBILIS
) {
    companion object {
        const val DEFAULT_MOBILIS = "644, 600, 606, Arseli, Mobilis, MOBILIS, أرسلي, موبيليس"
    }

    fun getMobilisTokens(): List<String> = splitTokens(mobilisSenders)

    private fun splitTokens(value: String): List<String> {
        return value.split(",", "،", ";", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
