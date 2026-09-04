package com.anomaly.currency.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Response shape of https://open.er-api.com/v6/latest/{base} — free, keyless. */
@Serializable
data class ErApiResponse(
    val result: String? = null,
    @SerialName("base_code") val baseCode: String? = null,
    @SerialName("time_last_update_unix") val updatedAtUnix: Long = 0L,
    val rates: Map<String, Double> = emptyMap(),
)

/** Where a single quote came from, in descending order of accuracy. */
enum class Provenance {
    /** Derived from the ECB daily reference feed. Matches Google/XE exactly. */
    Ecb,

    /** open.er-api.com aggregate. Used for currencies the ECB does not publish. */
    Aggregate,
}

/**
 * A USD-based rate table.
 *
 * [ecbCodes] records which quotes came from the ECB feed so the UI can tell the
 * user which numbers are benchmark-grade and which are aggregate estimates.
 */
data class RateTable(
    val rates: Map<String, Double>,
    val updatedAtMillis: Long,
    val ecbDate: String = "",
    val ecbCodes: Set<String> = emptySet(),
) {
    val base: String get() = "USD"

    val isEmpty: Boolean get() = rates.isEmpty()

    /**
     * Cross rate for [from] -> [to], triangulated through USD.
     * Returns null when either leg is missing.
     */
    fun rate(from: String, to: String): Double? {
        if (from == to) return 1.0
        val f = rates[from] ?: return null
        val t = rates[to] ?: return null
        if (f == 0.0) return null
        return t / f
    }

    /**
     * A pair is ECB-grade only when both legs are, since a cross rate inherits
     * the weaker of its two inputs.
     */
    fun provenanceOf(from: String, to: String): Provenance =
        if (from in ecbCodes && to in ecbCodes) Provenance.Ecb else Provenance.Aggregate

    companion object {
        /** Nothing fetched yet. The UI renders a loading state for this. */
        val Empty = RateTable(rates = emptyMap(), updatedAtMillis = 0L)
    }
}
