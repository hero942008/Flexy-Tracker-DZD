package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.util.CurrencyFormatter
import com.example.util.FlexySmsParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("حاسبة الفليكسي", appName)
  }

  @Test
  fun `test Mobilis sms parser`() {
    val sms = "Arseli: Vous avez reçu un rechargement de 1000 DA de 0661234567."
    val parsed = FlexySmsParser.parse("644", sms)
    assertNotNull(parsed)
    assertEquals(1000.0, parsed!!.amount, 0.01)
    assertEquals("Mobilis", parsed.operator)
  }

  @Test
  fun `test Djezzy sms parser`() {
    val sms = "Djezzy Flexy: Rechargement de 2000 DA effectué avec succès."
    val parsed = FlexySmsParser.parse("710", sms)
    assertNotNull(parsed)
    assertEquals(2000.0, parsed!!.amount, 0.01)
    assertEquals("Djezzy", parsed.operator)
  }

  @Test
  fun `test 10 percent calculation`() {
    val amount = 1000.0
    val percentage = 10.0
    val cut = (amount * percentage) / 100.0
    val net = amount - cut
    assertEquals(100.0, cut, 0.01)
    assertEquals(900.0, net, 0.01)
  }
}
