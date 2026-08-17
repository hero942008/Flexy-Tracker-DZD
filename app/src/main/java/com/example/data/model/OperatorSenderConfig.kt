package com.example.data.model

/**
 * Configuration for telecom operator SMS senders.
 * Users can customize the phone numbers / sender names (e.g. 644, 710, 555, 0661xxxxxx)
 * that each operator uses to deliver Flexy notifications.
 */
data class OperatorSenderConfig(
    val mobilisSenders: String = DEFAULT_MOBILIS,
    val djezzySenders: String = DEFAULT_DJEZZY,
    val ooredooSenders: String = DEFAULT_OOREDOO
) {
    companion object {
        const val DEFAULT_MOBILIS = "644, 600, Arseli, Mobilis"
        const val DEFAULT_DJEZZY = "710, 700, Flexy, Djezzy"
        const val DEFAULT_OOREDOO = "555, 500, Storm, Maxy, Ooredoo"
    }

    fun getMobilisTokens(): List<String> = splitTokens(mobilisSenders)
    fun getDjezzyTokens(): List<String> = splitTokens(djezzySenders)
    fun getOoredooTokens(): List<String> = splitTokens(ooredooSenders)

    private fun splitTokens(value: String): List<String> {
        return value.split(",", "،", ";", "\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
    }
}
