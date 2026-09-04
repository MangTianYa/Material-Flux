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

    /** Spot price from a precious-metals provider. */
    Metal,

    /** Digital-asset spot price from Coinbase. */
    Crypto,

    /** open.er-api.com aggregate. Used for currencies the ECB does not publish. */
    Aggregate,
}

/**
 * A USD-based rate table.
 *
 * The per-source code sets let the UI tell the user which numbers are
 * benchmark-grade and which are aggregate estimates or volatile spot prices.
 */
data class RateTable(
    val rates: Map<String, Double>,
    val updatedAtMillis: Long,
    val ecbDate: String = "",
    val ecbCodes: Set<String> = emptySet(),
    val metalCodes: Set<String> = emptySet(),
    val cryptoCodes: Set<String> = emptySet(),
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

    /** Provenance of a single leg. */
    fun provenanceOf(code: String): Provenance = when (code) {
        in ecbCodes -> Provenance.Ecb
        in metalCodes -> Provenance.Metal
        in cryptoCodes -> Provenance.Crypto
        else -> Provenance.Aggregate
    }

    /**
     * A pair inherits the weaker of its two legs, since a cross rate cannot be
     * more trustworthy than its inputs. Ordering follows the enum declaration.
     */
    fun provenanceOf(from: String, to: String): Provenance {
        val a = provenanceOf(from)
        val b = provenanceOf(to)
        return if (a.ordinal >= b.ordinal) a else b
    }

    companion object {
        /** Nothing fetched yet. The UI renders a loading state for this. */
        val Empty = RateTable(rates = emptyMap(), updatedAtMillis = 0L)
    }
}
