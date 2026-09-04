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
package com.anomaly.currency.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Digital-asset rates from Coinbase's public exchange-rates endpoint.
 *
 * The endpoint is keyless and already USD-based (`USD -> asset`), so no
 * re-basing is needed. It returns 600+ entries; we keep a curated subset so the
 * picker does not fill up with illiquid tokens.
 */
internal class CryptoApi(
    private val json: Json,
    private val client: OkHttpClient,
) {

    @Serializable
    private data class Envelope(val data: Payload = Payload())

    @Serializable
    private data class Payload(
        val currency: String? = null,
        val rates: Map<String, String> = emptyMap(),
    )

    /** USD -> asset rates for the curated set. Empty on failure. */
    suspend fun rates(): Map<String, Double> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(URL)
            .header("Accept", "application/json")
            .header("User-Agent", "MaterialFlux/1.2 (Android)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("crypto HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("crypto empty body")
            val parsed = json.decodeFromString<Envelope>(body)

            // Rates arrive as strings; drop anything unparseable rather than
            // letting a single malformed entry fail the whole refresh.
            SUPPORTED.mapNotNull { code ->
                val value = parsed.data.rates[code]?.toDoubleOrNull()
                if (value != null && value > 0.0 && value.isFinite()) code to value else null
            }.toMap()
        }
    }

    companion object {
        private const val URL = "https://api.coinbase.com/v2/exchange-rates?currency=USD"

        /** Curated by market capitalisation and liquidity. */
        val SUPPORTED = listOf(
            "BTC", "ETH", "USDT", "USDC", "XRP", "SOL", "DOGE", "ADA",
            "TRX", "LINK", "AVAX", "DOT", "BCH", "LTC", "XLM", "ATOM",
            "UNI", "ETC", "FIL", "AAVE",
        )
    }
}
