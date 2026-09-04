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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

/**
 * Spot prices for the four ISO 4217 precious-metal codes.
 *
 * api.gold-api.com is keyless and quotes USD per troy ounce, so a USD-based
 * rate is the reciprocal. Each metal is a separate request; a failure on one
 * does not drop the others.
 */
internal class MetalsApi(
    private val json: Json,
    private val client: OkHttpClient,
) {

    @Serializable
    private data class MetalQuote(
        val symbol: String? = null,
        val price: Double = 0.0,
        @SerialName("updatedAt") val updatedAt: String? = null,
    )

    /** USD -> metal rates, keyed by ISO code. Empty when every request fails. */
    suspend fun spot(): Map<String, Double> = coroutineScope {
        SYMBOLS
            .map { symbol -> async { runCatching { fetch(symbol) }.getOrNull() } }
            .mapNotNull { it.await() }
            .toMap()
    }

    private suspend fun fetch(symbol: String): Pair<String, Double> =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url("https://api.gold-api.com/price/$symbol")
                .header("Accept", "application/json")
                .header("User-Agent", "MaterialFlux/1.2 (Android)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("metals HTTP ${response.code} for $symbol")
                }
                val body = response.body?.string()
                    ?: throw IOException("metals empty body for $symbol")
                val quote = json.decodeFromString<MetalQuote>(body)
                if (quote.price <= 0.0 || !quote.price.isFinite()) {
                    throw IOException("metals bad price for $symbol")
                }
                // Provider quotes USD per ounce; we need USD -> metal.
                symbol to (1.0 / quote.price)
            }
        }

    private companion object {
        val SYMBOLS = listOf("XAU", "XAG", "XPT", "XPD")
    }
}
