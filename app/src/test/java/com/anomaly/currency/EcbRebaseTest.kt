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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the ECB re-basing arithmetic against the real published feed values.
 *
 * This is the fix for the discrepancy the user observed against Google: providers
 * that ship a pre-computed USD base round to 4–5 significant digits, so USD->CNY
 * arrives as 6.7191 instead of 6.719070. Dividing the EUR legs ourselves keeps
 * the full precision.
 */
class EcbRebaseTest {

    // Real ECB eurofxref-daily.xml values for 2026-09-03 (EUR base).
    private val eurBase = mapOf(
        "USD" to 1.1615,
        "CNY" to 7.8042,
        "JPY" to 181.21,
        "GBP" to 0.86055,
        "CHF" to 0.939,
        "KRW" to 1576.49,
        "EUR" to 1.0,
    )

    private fun toUsdBase(): Map<String, Double> {
        val eurUsd = eurBase.getValue("USD")
        return eurBase.mapValues { (_, v) -> v / eurUsd }
    }

    @Test
    fun `USD leg re-bases to exactly one`() {
        assertEquals(1.0, toUsdBase().getValue("USD"), 1e-12)
    }

    @Test
    fun `CNY re-bases to the full-precision cross rate`() {
        // 7.8042 / 1.1615 — this is the number Google and XE display.
        assertEquals(6.71907017, toUsdBase().getValue("CNY"), 1e-8)
    }

    @Test
    fun `re-based CNY differs from a provider's rounded value`() {
        val ours = toUsdBase().getValue("CNY")
        val roundedProvider = 6.7191
        // The gap is small but it is exactly what the user saw against Google.
        assertTrue(ours != roundedProvider)
        assertTrue(Math.abs(ours - roundedProvider) < 1e-3)
    }

    @Test
    fun `aggregate CNY is measurably further from ECB than our cross rate`() {
        val ecb = toUsdBase().getValue("CNY")
        // open.er-api.com served 6.73552 for the same day: ~24 bp away, because
        // it blends onshore CNY with offshore CNH.
        val aggregate = 6.73552
        val deviationBp = Math.abs((aggregate - ecb) / ecb) * 10_000
        assertTrue("expected >20bp, got $deviationBp", deviationBp > 20.0)
    }

    @Test
    fun `all legs re-base consistently`() {
        val usd = toUsdBase()
        assertEquals(156.01377529, usd.getValue("JPY"), 1e-8)
        assertEquals(0.74089539, usd.getValue("GBP"), 1e-8)
        assertEquals(0.80843737, usd.getValue("CHF"), 1e-8)
        assertEquals(0.86095566, usd.getValue("EUR"), 1e-8)
        assertEquals(1357.28798967, usd.getValue("KRW"), 1e-8)
    }

    @Test
    fun `cross rates are independent of the base currency`() {
        val usd = toUsdBase()
        // EUR->CNY computed via USD must equal the raw EUR-based quote.
        val viaUsd = usd.getValue("CNY") / usd.getValue("EUR")
        assertEquals(eurBase.getValue("CNY"), viaUsd, 1e-9)
    }
}
