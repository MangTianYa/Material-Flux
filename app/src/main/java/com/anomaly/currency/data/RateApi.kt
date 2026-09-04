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
 * Builds the rate table from two sources and merges them by accuracy.
 *
 * The ECB daily reference feed is the benchmark Google and XE display, but it
 * only covers ~30 currencies. open.er-api.com covers 166 but its CNY quote sits
 * ~0.24% away from the ECB cross rate — measured, not assumed — because it
 * blends onshore and offshore Renminbi.
 *
 * So: ECB wins wherever it publishes, aggregate fills the long tail.
 */
internal class RateApi(
    private val json: Json,
    private val client: OkHttpClient = defaultClient(),
) {

    private val ecb = EcbApi(client)

    /**
     * Fetches both sources concurrently and merges them.
     *
     * Neither source is allowed to fail the whole refresh on its own: if the ECB
     * feed is unreachable we still return the aggregate table, and vice versa.
     * Only a total failure throws.
     */
    suspend fun latest(): RateTable = coroutineScope {
        val ecbDeferred = async { runCatching { ecb.daily() } }
        val aggregateDeferred = async { runCatching { aggregate() } }

        val ecbResult = ecbDeferred.await()
        val aggregateResult = aggregateDeferred.await()

        val ecbSnapshot = ecbResult.getOrNull()
        val aggregate = aggregateResult.getOrNull()

        if (ecbSnapshot == null && aggregate == null) {
            throw aggregateResult.exceptionOrNull()
                ?: ecbResult.exceptionOrNull()
                ?: IOException("both rate providers failed")
        }

        val ecbUsd = ecbSnapshot?.toUsdBase() ?: emptyMap()

        // Aggregate first, then overwrite with ECB so ECB always wins a conflict.
        val merged = LinkedHashMap<String, Double>(aggregate?.rates.orEmpty())
        merged.putAll(ecbUsd)
        merged["USD"] = 1.0

        RateTable(
            rates = merged,
            updatedAtMillis = aggregate?.updatedAtMillis
                ?.takeIf { it > 0 }
                ?: System.currentTimeMillis(),
            ecbDate = ecbSnapshot?.date.orEmpty(),
            ecbCodes = ecbUsd.keys,
        )
    }

    /** open.er-api.com — keyless, 166 currencies, refreshed daily. */
    private suspend fun aggregate(): AggregateSnapshot = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AGGREGATE_URL)
            .header("Accept", "application/json")
            .header("User-Agent", "CurrencyM3/1.0 (Android)")
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
