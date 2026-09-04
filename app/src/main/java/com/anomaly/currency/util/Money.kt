package com.anomaly.currency.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Money formatting helpers. Conversion goes through [BigDecimal] so repeated
 * edits never accumulate binary floating-point drift in the visible amount.
 */
object Money {

    /** Fixed precision for every displayed rate, per product requirement. */
    const val RATE_DECIMALS: Int = 6

    private val symbols = DecimalFormatSymbols(Locale.US)
    private val grouping = DecimalFormat("#,##0.##", symbols)

    /** Grouped output with exactly [decimals] fraction digits. */
    fun format(value: BigDecimal, decimals: Int): String {
        val scaled = value.setScale(decimals, RoundingMode.HALF_UP)
        return synchronized(grouping) {
            grouping.minimumFractionDigits = decimals
            grouping.maximumFractionDigits = decimals
            grouping.format(scaled)
        }
    }

    /**
     * Rate line formatting, fixed at six decimals.
     *
     * The rate is a quotient we compute ourselves from the source quote, so the
     * six digits carry real information rather than trailing zeros — for
     * USD->CNY the ECB cross rate is 6.719070, not 6.7191.
     *
     * Below [SIGNIFICANT_THRESHOLD] six decimals would leave two or fewer
     * significant digits (JPY -> KWD is ~0.0000019), so those switch to
     * significant-digit notation instead of rendering as 0.000002.
     */
    fun formatRate(rate: Double): String {
        if (rate <= 0.0 || !rate.isFinite()) return "—"
        val value = BigDecimal(rate)
        if (rate < SIGNIFICANT_THRESHOLD) {
            return value.round(MathContext(3)).stripTrailingZeros().toPlainString()
        }
        return format(value, RATE_DECIMALS)
    }

    /** Parses editor text, tolerating grouping separators and a bare "." */
    fun parse(input: String): BigDecimal? {
        if (input.isBlank()) return BigDecimal.ZERO
        val cleaned = input.replace(",", "").replace("\u00A0", "").trim()
        if (cleaned == "." || cleaned == "-") return BigDecimal.ZERO
        return runCatching { BigDecimal(cleaned) }.getOrNull()
    }

    /** Converts [amount] using a plain multiplier, rounded for display. */
    fun convert(amount: BigDecimal, rate: Double, decimals: Int): BigDecimal =
        amount.multiply(BigDecimal(rate)).setScale(decimals, RoundingMode.HALF_UP)

    /**
     * Below this, six decimals would leave two or fewer significant digits, so
     * [formatRate] switches to significant-digit notation.
     */
    private const val SIGNIFICANT_THRESHOLD = 0.0001
}
