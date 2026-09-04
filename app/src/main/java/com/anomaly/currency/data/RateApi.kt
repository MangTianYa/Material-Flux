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
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Builds the rate table from four sources, merged by accuracy.
 *
 * The ECB daily reference feed is the benchmark Google and XE display, but it
 * only covers ~30 currencies. open.er-api.com covers 166 fiat currencies but its
 * CNY quote sits ~0.24% away from the ECB cross rate — measured, not assumed —
 * because it blends onshore and offshore Renminbi.
 *
 * Precious metals and digital assets come from dedicated providers since no
 * fiat feed quotes them reliably.
 *
 * Merge order: aggregate -> crypto -> metals -> ECB. ECB always wins.
 */
internal class RateApi(
    private val json: Json,
    private val client: OkHttpClient = defaultClient(),
) {

    private val ecb = EcbApi(client)
    private val metals = MetalsApi(json, client)
    private val crypto = CryptoApi(json, client)

    /**
     * Fetches every source concurrently and merges them.
     *
     * Only a total failure of both fiat sources throws; metals and crypto are
     * strictly additive, so their failure just means those rows are absent.
     */
    suspend fun latest(): RateTable = coroutineScope {
        val ecbDeferred = async { runCatching { ecb.daily() } }
        val aggregateDeferred = async { runCatching { aggregate() } }
        val metalsDeferred = async { runCatching { metals.spot() }.getOrDefault(emptyMap()) }
        val cryptoDeferred = async { runCatching { crypto.rates() }.getOrDefault(emptyMap()) }

        val ecbResult = ecbDeferred.await()
        val aggregateResult = aggregateDeferred.await()
        val metalRates = metalsDeferred.await()
        val cryptoRates = cryptoDeferred.await()

        val ecbSnapshot = ecbResult.getOrNull()
        val aggregate = aggregateResult.getOrNull()

        if (ecbSnapshot == null && aggregate == null) {
            throw aggregateResult.exceptionOrNull()
                ?: ecbResult.exceptionOrNull()
                ?: IOException("both fiat rate providers failed")
        }

        val ecbUsd = ecbSnapshot?.toUsdBase() ?: emptyMap()

        val merged = LinkedHashMap<String, Double>(aggregate?.rates.orEmpty())
        merged.putAll(cryptoRates)
        merged.putAll(metalRates)
        merged.putAll(ecbUsd)
        merged["USD"] = 1.0

        RateTable(
            rates = merged,
            updatedAtMillis = aggregate?.updatedAtMillis
                ?.takeIf { it > 0 }
                ?: System.currentTimeMillis(),
            ecbDate = ecbSnapshot?.date.orEmpty(),
            ecbCodes = ecbUsd.keys,
            metalCodes = metalRates.keys,
            cryptoCodes = cryptoRates.keys,
        )
    }

    /** open.er-api.com — keyless, 166 fiat currencies, refreshed daily. */
    private suspend fun aggregate(): AggregateSnapshot = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AGGREGATE_URL)
            .header("Accept", "application/json")
            .header("User-Agent", "MaterialFlux/1.2 (Android)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("aggregate HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("aggregate empty body")
            val parsed = json.decodeFromString<ErApiResponse>(body)
            if (parsed.result != null && parsed.result != "success") {
                throw IOException("aggregate result=${parsed.result}")
            }
            val rates = parsed.rates.filterValues { it > 0.0 && it.isFinite() }
            if (rates.isEmpty()) throw IOException("aggregate returned no quotes")
            AggregateSnapshot(
                rates = if ("USD" in rates) rates else rates + ("USD" to 1.0),
                updatedAtMillis = parsed.updatedAtUnix.takeIf { it > 0 }?.times(1000) ?: 0L,
            )
        }
    }

    private data class AggregateSnapshot(
        val rates: Map<String, Double>,
        val updatedAtMillis: Long,
    )

    companion object {
        private const val AGGREGATE_URL = "https://open.er-api.com/v6/latest/USD"

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .callTimeout(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }
}
