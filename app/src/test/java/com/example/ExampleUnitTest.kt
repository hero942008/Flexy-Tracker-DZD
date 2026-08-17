package com.example

import com.example.data.model.OperatorSenderConfig
import com.example.util.FlexySmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultParser_detectsMobilis() {
        val parsed = FlexySmsParser.parse("644", "Arseli: Vous avez reçu un rechargement de 1000 DA")
        assertNotNull(parsed)
        assertEquals(1000.0, parsed?.amount)
        assertEquals("Mobilis", parsed?.operator)
    }

    @Test
    fun defaultParser_detectsDjezzy() {
        val parsed = FlexySmsParser.parse("710", "Djezzy Flexy: Rechargement de 2000 DA")
        assertNotNull(parsed)
        assertEquals(2000.0, parsed?.amount)
        assertEquals("Djezzy", parsed?.operator)
    }

    @Test
    fun defaultParser_detectsOoredoo() {
        val parsed = FlexySmsParser.parse("555", "Storm: Vous avez reçu 500 DA")
        assertNotNull(parsed)
        assertEquals(500.0, parsed?.amount)
        assertEquals("Ooredoo", parsed?.operator)
    }

    @Test
    fun customConfig_customPhoneNumberAttribution() {
        val config = OperatorSenderConfig(
            mobilisSenders = "0661998877, 644",
            djezzySenders = "0770112233, 710",
            ooredooSenders = "0550445566, 555"
        )

        val mobilisCustom = FlexySmsParser.parse("0661998877", "Recharge de 1500 DA", config)
        assertNotNull(mobilisCustom)
        assertEquals("Mobilis", mobilisCustom?.operator)
        assertEquals(1500.0, mobilisCustom?.amount)

        val djezzyCustom = FlexySmsParser.parse("0770112233", "Recharge de 3000 DA", config)
        assertNotNull(djezzyCustom)
        assertEquals("Djezzy", djezzyCustom?.operator)
        assertEquals(3000.0, djezzyCustom?.amount)

        val ooredooCustom = FlexySmsParser.parse("0550445566", "Recharge de 500 DA", config)
        assertNotNull(ooredooCustom)
        assertEquals("Ooredoo", ooredooCustom?.operator)
        assertEquals(500.0, ooredooCustom?.amount)
    }
}

