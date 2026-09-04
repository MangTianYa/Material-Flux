package com.anomaly.currency

import com.anomaly.currency.data.ErApiResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Parses a payload shaped exactly like the live open.er-api.com response, using
 * the production DTO, so a provider field rename fails the build instead of
 * silently shipping "—" in the UI.
 */
class RateParsingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val payload = """
        {
          "result": "success",
          "provider": "https://www.exchangerate-api.com",
          "documentation": "https://www.exchangerate-api.com/docs/free",
          "time_last_update_unix": 1788480151,
          "time_last_update_utc": "Fri, 04 Sep 2026 00:02:31 +0000",
          "time_next_update_unix": 1788566781,
          "base_code": "USD",
          "rates": { "USD": 1, "CNY": 6.73552, "JPY": 156.019704, "EUR": 0.860629 }
        }
    """.trimIndent()

    @Test
    fun `parses the live response shape`() {
        val response = json.decodeFromString<ErApiResponse>(payload)
        assertEquals("success", response.result)
        assertEquals("USD", response.baseCode)
        assertEquals(1788480151L, response.updatedAtUnix)
        assertEquals(6.73552, response.rates.getValue("CNY"), 1e-9)
    }

    @Test
    fun `unknown fields do not break parsing`() {
        val withExtra = payload.replace(
            "\"base_code\": \"USD\"",
            "\"base_code\": \"USD\", \"brand_new_field\": [1,2,3]",
        )
        val response = json.decodeFromString<ErApiResponse>(withExtra)
        assertEquals(4, response.rates.size)
    }

    @Test
    fun `missing optional fields fall back to defaults`() {
        val minimal = """{ "rates": { "USD": 1, "CNY": 6.7 } }"""
        val response = json.decodeFromString<ErApiResponse>(minimal)
        assertEquals(null, response.result)
        assertEquals(0L, response.updatedAtUnix)
        assertTrue(response.rates.containsKey("CNY"))
    }
}
