package com.example

import com.example.data.model.OperatorSenderConfig
import com.example.util.FlexySmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun defaultParser_detectsOfficialMobilisFlexyMessage() {
        val rawSms = "Vous avez rechargé 100.00 DZD DA avec succès le 21/08/2026 10:38:46. Profitez d’une multitude d’avantages avec les nouvelles offres MOBILIS."
        val parsed = FlexySmsParser.parse("644", rawSms)
        assertNotNull(parsed)
        assertEquals(100.0, parsed?.amount)
        assertEquals("Mobilis", parsed?.operator)
    }

    @Test
    fun defaultParser_detectsArseliMobilis() {
        val parsed = FlexySmsParser.parse("Arseli", "Arseli: Vous avez reçu un rechargement de 1000 DA")
        assertNotNull(parsed)
        assertEquals(1000.0, parsed?.amount)
        assertEquals("Mobilis", parsed?.operator)
    }

    @Test
    fun defaultParser_rejectsPersonalSms() {
        // Normal contact sending random number or text
        val parsed = FlexySmsParser.parse("TAHER", "3567")
        assertNull(parsed)

        val phoneSms = FlexySmsParser.parse("0661234567", "1000")
        assertNull(phoneSms)
    }

    @Test
    fun defaultParser_rejectsNonMobilisMessage() {
        val nonMobilis = FlexySmsParser.parse("Ooredoo", "Solde 500 DA")
        assertNull(nonMobilis)
    }
}


