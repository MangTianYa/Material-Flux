package com.anomaly.currency.data

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.IOException

/**
 * European Central Bank daily reference rates.
 *
 * This is the benchmark Google, XE and Wise all display. The feed is EUR-based
 * and published on TARGET business days around 16:00 CET; the payload is ~1.5KB
 * and carries `Cache-Control: max-age=300`.
 *
 * We parse it with the framework [XmlPullParser] rather than pulling in an XML
 * library — the document is a flat three-level tree.
 */
internal class EcbApi(private val client: OkHttpClient) {

    suspend fun daily(): EcbSnapshot = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(FEED_URL)
            .header("Accept", "application/xml")
            .header("User-Agent", "CurrencyM3/1.0 (Android)")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("ECB HTTP ${response.code}")
            }
            val body = response.body ?: throw IOException("ECB empty body")
            parse(body.charStream())
        }
    }

    private fun parse(reader: java.io.Reader): EcbSnapshot {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(reader)

        var date = ""
        val rates = LinkedHashMap<String, Double>()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.name == "Cube") {
                // Three Cube shapes share one tag name:
                //   <Cube>                     -> container
                //   <Cube time='…'>            -> the observation date
                //   <Cube currency='X' rate='…'> -> one quote
                parser.getAttributeValue(null, "time")?.let { date = it }

                val code = parser.getAttributeValue(null, "currency")
                val rate = parser.getAttributeValue(null, "rate")?.toDoubleOrNull()
                if (code != null && rate != null && rate > 0.0) {
                    rates[code] = rate
                }
            }
            event = parser.next()
        }

        if (rates.isEmpty()) throw IOException("ECB returned no quotes")
        if (!rates.containsKey("USD")) throw IOException("ECB feed missing USD leg")

        // EUR is the implicit base and never appears as a Cube.
        rates["EUR"] = 1.0

        return EcbSnapshot(date = date, eurBase = rates)
    }

    private companion object {
        const val FEED_URL = "https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml"
    }
}

/** Raw ECB observation: EUR -> X for ~30 currencies. */
internal data class EcbSnapshot(
    val date: String,
    val eurBase: Map<String, Double>,
) {
    /**
     * Re-bases to USD by dividing every leg by EUR->USD.
     *
     * Doing the division ourselves in [Double] is the whole point: providers that
     * pre-compute a USD base round the result to 4–5 significant digits, which is
     * where the visible discrepancy against Google comes from.
     */
    fun toUsdBase(): Map<String, Double> {
        val eurUsd = eurBase.getValue("USD")
        return eurBase.mapValues { (_, eurRate) -> eurRate / eurUsd }
    }
}
