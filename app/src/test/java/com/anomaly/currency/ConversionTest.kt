/*
 * Material Flux — Material 3 currency converter for Android
 * Copyright (C) 2026 MangTianYa
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package com.anomaly.currency

import com.anomaly.currency.data.Currencies
import com.anomaly.currency.data.Provenance
import com.anomaly.currency.data.RateTable
import com.anomaly.currency.data.Region
import com.anomaly.currency.util.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ConversionTest {

    private val table = RateTable(
        rates = mapOf(
            "USD" to 1.0,
            "CNY" to 7.8042 / 1.1615,
            "JPY" to 181.21 / 1.1615,
            "EUR" to 1.0 / 1.1615,
            "XYZ" to 12.5,
            "XAU" to 1.0 / 4473.600098,
            "BTC" to 0.0000123643690537,
        ),
        updatedAtMillis = 0L,
        ecbDate = "2026-09-03",
        ecbCodes = setOf("USD", "CNY", "JPY", "EUR"),
        metalCodes = setOf("XAU"),
        cryptoCodes = setOf("BTC"),
    )

    @Test
    fun `identity rate is one`() {
        assertEquals(1.0, table.rate("USD", "USD")!!, 0.0)
        assertEquals(1.0, table.rate("CNY", "CNY")!!, 0.0)
    }

    @Test
    fun `cross rate triangulates through USD`() {
        // 1 CNY = 156.01377529 / 6.71907017 JPY
        assertEquals(23.2195, table.rate("CNY", "JPY")!!, 1e-4)
    }

    @Test
    fun `inverse of a cross rate round-trips`() {
        val forward = table.rate("EUR", "JPY")!!
        val back = table.rate("JPY", "EUR")!!
        assertEquals(1.0, forward * back, 1e-9)
    }

    @Test
    fun `unknown code yields null instead of throwing`() {
        assertNull(table.rate("USD", "ZZZ"))
        assertNull(table.rate("ZZZ", "USD"))
    }

    @Test
    fun `empty table reports blank and yields no rates`() {
        assertTrue(RateTable.Empty.isEmpty)
        assertNull(RateTable.Empty.rate("USD", "CNY"))
    }

    // ── Provenance ────────────────────────────────────────────────────────────

    @Test
    fun `single leg reports its own source`() {
        assertEquals(Provenance.Ecb, table.provenanceOf("CNY"))
        assertEquals(Provenance.Metal, table.provenanceOf("XAU"))
        assertEquals(Provenance.Crypto, table.provenanceOf("BTC"))
        assertEquals(Provenance.Aggregate, table.provenanceOf("XYZ"))
        // Codes absent from every set degrade rather than throw.
        assertEquals(Provenance.Aggregate, table.provenanceOf("ZZZ"))
    }

    @Test
    fun `pair inherits the weaker of its two legs`() {
        assertEquals(Provenance.Ecb, table.provenanceOf("USD", "CNY"))
        assertEquals(Provenance.Ecb, table.provenanceOf("CNY", "JPY"))
        // A metal or crypto leg outranks ECB in the degradation order.
        assertEquals(Provenance.Metal, table.provenanceOf("USD", "XAU"))
        assertEquals(Provenance.Metal, table.provenanceOf("XAU", "CNY"))
        assertEquals(Provenance.Crypto, table.provenanceOf("USD", "BTC"))
        // Aggregate is the weakest, so it wins against anything.
        assertEquals(Provenance.Aggregate, table.provenanceOf("XYZ", "CNY"))
        assertEquals(Provenance.Aggregate, table.provenanceOf("BTC", "XYZ"))
    }

    @Test
    fun `metals and crypto participate in cross rates`() {
        // 1 BTC in CNY: both legs present, so the quotient is defined.
        val btcCny = table.rate("BTC", "CNY")
        assertNotNull(btcCny)
        assertTrue("expected a large number, got $btcCny", btcCny!! > 100_000.0)

        // 1 oz gold in USD should be the provider's spot price.
        assertEquals(4473.600098, table.rate("XAU", "USD")!!, 1e-6)
    }

    // ── Rate formatting: six decimals ─────────────────────────────────────────

    @Test
    fun `rate renders exactly six decimals`() {
        assertEquals("6.719070", Money.formatRate(7.8042 / 1.1615))
        assertEquals("156.013775", Money.formatRate(181.21 / 1.1615))
        assertEquals("0.860956", Money.formatRate(1.0 / 1.1615))
    }

    @Test
    fun `six decimals preserve the digits a rounded provider would drop`() {
        // A provider serving a pre-computed USD base returns 6.7191; computing
        // the cross rate ourselves keeps the real 6.719070.
        val ours = Money.formatRate(7.8042 / 1.1615)
        assertEquals("6.719070", ours)
        assertFalse(ours == "6.719100")
    }

    @Test
    fun `very small rates fall back to significant digits`() {
        // Fixed six decimals would collapse these to 0.000002 / 0.000000.
        assertEquals("0.0000019", Money.formatRate(0.0000019))
        assertEquals("0.00000019", Money.formatRate(0.00000019))
        // JPY -> KWD is the real-world case: ~0.00001977
        assertEquals("0.0000198", Money.formatRate(0.000019773))
    }

    @Test
    fun `rates above the threshold keep fixed six decimals`() {
        assertEquals("0.000212", Money.formatRate(0.000212244))
        assertEquals("0.001977", Money.formatRate(0.0019773))
    }

    @Test
    fun `non-finite and non-positive rates render as a dash`() {
        assertEquals("—", Money.formatRate(0.0))
        assertEquals("—", Money.formatRate(-1.0))
        assertEquals("—", Money.formatRate(Double.NaN))
        assertEquals("—", Money.formatRate(Double.POSITIVE_INFINITY))
    }

    @Test
    fun `large rates keep grouping and six decimals`() {
        assertEquals("1,357.287990", Money.formatRate(1357.28798967))
    }

    // ── Amount formatting: per-currency minor units ────────────────────────────

    @Test
    fun `zero-decimal currencies render without a fraction`() {
        val jpy = Currencies.resolve("JPY")
        assertEquals(0, jpy.decimals)
        val converted = Money.convert(BigDecimal("10"), 181.21 / 1.1615, jpy.decimals)
        assertEquals("1,560", Money.format(converted, jpy.decimals))
    }

    @Test
    fun `three-decimal currencies keep all digits`() {
        val kwd = Currencies.resolve("KWD")
        assertEquals(3, kwd.decimals)
        assertEquals("0.308", Money.format(BigDecimal("0.308482"), kwd.decimals))
    }

    @Test
    fun `parse tolerates grouping separators`() {
        assertEquals(BigDecimal("1234.56"), Money.parse("1,234.56"))
        assertEquals(BigDecimal.ZERO, Money.parse(""))
        assertEquals(BigDecimal.ZERO, Money.parse("."))
        assertNull(Money.parse("abc"))
    }

    // ── Currency metadata ─────────────────────────────────────────────────────

    @Test
    fun `currency list covers every provider code without duplicates`() {
        val codes = Currencies.all.map { it.code }
        // 166 fiat + 4 precious metals + 20 digital assets
        assertEquals(190, codes.size)
        assertEquals(190, codes.toSet().size)
    }

    @Test
    fun `metals and crypto are grouped into their own regions`() {
        val metals = Currencies.all.filter { it.region == Region.Metal }.map { it.code }
        assertEquals(listOf("XAU", "XAG", "XPT", "XPD").sorted(), metals.sorted())

        val crypto = Currencies.all.filter { it.region == Region.Crypto }
        assertEquals(20, crypto.size)
        // Volatile assets need more precision than fiat's two decimals.
        assertTrue(crypto.all { it.decimals >= 4 })
    }

    @Test
    fun `every currency has a searchable key covering code and both names`() {
        for (c in Currencies.all) {
            assertTrue("${c.code} missing code in searchKey", c.searchKey.contains(c.code.lowercase()))
            assertTrue("${c.code} missing zh name", c.searchKey.contains(c.nameZh.lowercase()))
            assertTrue("${c.code} missing en name", c.searchKey.contains(c.name.lowercase()))
        }
    }

    @Test
    fun `default favorites are all known currencies`() {
        for (code in Currencies.defaultFavorites) {
            assertNotNull("missing metadata for $code", Currencies[code])
        }
    }

    @Test
    fun `unknown codes resolve to a usable placeholder`() {
        val c = Currencies.resolve("QQQ")
        assertEquals("QQQ", c.code)
        assertEquals("", Currencies.flagEmoji(c.flag))
    }

    @Test
    fun `flag emoji is a surrogate pair for valid regions`() {
        assertEquals(4, Currencies.flagEmoji("CN").length)
        assertEquals("", Currencies.flagEmoji("XX"))
        assertEquals("", Currencies.flagEmoji("1"))
    }
}
