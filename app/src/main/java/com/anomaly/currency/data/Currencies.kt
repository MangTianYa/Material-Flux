package com.anomaly.currency.data

import androidx.compose.runtime.Immutable

/**
 * Geographic grouping used to lay out the currency picker. Ordering of the enum
 * is the display order of the sections.
 */
enum class Region(val labelZh: String) {
    Asia("亚洲"),
    Europe("欧洲"),
    Americas("美洲"),
    MiddleEast("中东"),
    Africa("非洲"),
    Oceania("大洋洲"),
    Metal("贵金属"),
    Crypto("数字货币"),
    Other("其他"),
}

/**
 * Static currency metadata: display name, symbol, decimal precision, region and
 * the country used to render a flag.
 *
 * Kept in-app rather than fetched so the picker can render, search and group
 * instantly. [decimals] follows the ISO 4217 minor-unit exponent.
 *
 * [flag] is a two-letter region code converted to a regional-indicator emoji at
 * render time, which avoids shipping ~170 flag drawables.
 */
@Immutable
data class Currency(
    val code: String,
    val name: String,
    val nameZh: String,
    val symbol: String,
    val flag: String,
    val region: Region,
    val decimals: Int = 2,
    /** Lower-cased search haystack, precomputed once at class-init. */
    val searchKey: String = "",
)

object Currencies {

    private fun c(
        code: String,
        name: String,
        nameZh: String,
        symbol: String,
        flag: String,
        region: Region,
        decimals: Int = 2,
    ) = Currency(
        code = code,
        name = name,
        nameZh = nameZh,
        symbol = symbol,
        flag = flag,
        region = region,
        decimals = decimals,
        searchKey = "$code $name $nameZh $symbol".lowercase(),
    )

    /**
     * All 166 currencies served by open.er-api.com, verified against a live
     * response. Codes the API stops serving are filtered out at runtime by the
     * picker, so this list only ever over-covers.
     */
    val all: List<Currency> = listOf(
        // ── Asia ──────────────────────────────────────────────────────────────
        c("AFN", "Afghan Afghani", "阿富汗尼", "؋", "AF", Region.Asia),
        c("AMD", "Armenian Dram", "亚美尼亚德拉姆", "֏", "AM", Region.Asia),
        c("AZN", "Azerbaijani Manat", "阿塞拜疆马纳特", "₼", "AZ", Region.Asia),
        c("BDT", "Bangladeshi Taka", "孟加拉塔卡", "৳", "BD", Region.Asia),
        c("BND", "Brunei Dollar", "文莱元", "B$", "BN", Region.Asia),
        c("BTN", "Bhutanese Ngultrum", "不丹努尔特鲁姆", "Nu.", "BT", Region.Asia),
        c("CNH", "Chinese Yuan (Offshore)", "人民币（离岸）", "¥", "CN", Region.Asia),
        c("CNY", "Chinese Yuan", "人民币", "¥", "CN", Region.Asia),
        c("GEL", "Georgian Lari", "格鲁吉亚拉里", "₾", "GE", Region.Asia),
        c("HKD", "Hong Kong Dollar", "港元", "HK$", "HK", Region.Asia),
        c("IDR", "Indonesian Rupiah", "印尼卢比", "Rp", "ID", Region.Asia),
        c("INR", "Indian Rupee", "印度卢比", "₹", "IN", Region.Asia),
        c("JPY", "Japanese Yen", "日元", "¥", "JP", Region.Asia, 0),
        c("KGS", "Kyrgystani Som", "吉尔吉斯索姆", "с", "KG", Region.Asia),
        c("KHR", "Cambodian Riel", "柬埔寨瑞尔", "៛", "KH", Region.Asia),
        c("KRW", "South Korean Won", "韩元", "₩", "KR", Region.Asia, 0),
        c("KZT", "Kazakhstani Tenge", "哈萨克坚戈", "₸", "KZ", Region.Asia),
        c("LAK", "Lao Kip", "老挝基普", "₭", "LA", Region.Asia),
        c("LKR", "Sri Lankan Rupee", "斯里兰卡卢比", "Rs", "LK", Region.Asia),
        c("MMK", "Myanmar Kyat", "缅甸元", "K", "MM", Region.Asia),
        c("MNT", "Mongolian Tugrik", "蒙古图格里克", "₮", "MN", Region.Asia),
        c("MOP", "Macanese Pataca", "澳门元", "MOP$", "MO", Region.Asia),
        c("MVR", "Maldivian Rufiyaa", "马尔代夫拉菲亚", ".ރ", "MV", Region.Asia),
        c("MYR", "Malaysian Ringgit", "马来西亚林吉特", "RM", "MY", Region.Asia),
        c("NPR", "Nepalese Rupee", "尼泊尔卢比", "Rs", "NP", Region.Asia),
        c("PHP", "Philippine Peso", "菲律宾比索", "₱", "PH", Region.Asia),
        c("PKR", "Pakistani Rupee", "巴基斯坦卢比", "₨", "PK", Region.Asia),
        c("SGD", "Singapore Dollar", "新加坡元", "S$", "SG", Region.Asia),
        c("THB", "Thai Baht", "泰铢", "฿", "TH", Region.Asia),
        c("TJS", "Tajikistani Somoni", "塔吉克索莫尼", "ЅМ", "TJ", Region.Asia),
        c("TMT", "Turkmenistani Manat", "土库曼马纳特", "m", "TM", Region.Asia),
        c("TWD", "New Taiwan Dollar", "新台币", "NT$", "TW", Region.Asia),
        c("UZS", "Uzbekistani Som", "乌兹别克苏姆", "so'm", "UZ", Region.Asia),
        c("VND", "Vietnamese Dong", "越南盾", "₫", "VN", Region.Asia, 0),

        // ── Europe ────────────────────────────────────────────────────────────
        c("ALL", "Albanian Lek", "阿尔巴尼亚列克", "L", "AL", Region.Europe),
        c("BAM", "Bosnia-Herzegovina Mark", "波黑可兑换马克", "KM", "BA", Region.Europe),
        c("BGN", "Bulgarian Lev", "保加利亚列弗", "лв", "BG", Region.Europe),
        c("BYN", "Belarusian Ruble", "白俄罗斯卢布", "Br", "BY", Region.Europe),
        c("CHF", "Swiss Franc", "瑞士法郎", "CHF", "CH", Region.Europe),
        c("CZK", "Czech Koruna", "捷克克朗", "Kč", "CZ", Region.Europe),
        c("DKK", "Danish Krone", "丹麦克朗", "kr", "DK", Region.Europe),
        c("EUR", "Euro", "欧元", "€", "EU", Region.Europe),
        c("FOK", "Faroese Króna", "法罗群岛克朗", "kr", "FO", Region.Europe),
        c("GBP", "British Pound", "英镑", "£", "GB", Region.Europe),
        c("GGP", "Guernsey Pound", "根西镑", "£", "GG", Region.Europe),
        c("GIP", "Gibraltar Pound", "直布罗陀镑", "£", "GI", Region.Europe),
        c("HRK", "Croatian Kuna", "克罗地亚库纳", "kn", "HR", Region.Europe),
        c("HUF", "Hungarian Forint", "匈牙利福林", "Ft", "HU", Region.Europe),
        c("IMP", "Manx Pound", "马恩岛镑", "£", "IM", Region.Europe),
        c("ISK", "Icelandic Króna", "冰岛克朗", "kr", "IS", Region.Europe, 0),
        c("JEP", "Jersey Pound", "泽西镑", "£", "JE", Region.Europe),
        c("MDL", "Moldovan Leu", "摩尔多瓦列伊", "L", "MD", Region.Europe),
        c("MKD", "Macedonian Denar", "北马其顿代纳尔", "ден", "MK", Region.Europe),
        c("NOK", "Norwegian Krone", "挪威克朗", "kr", "NO", Region.Europe),
        c("PLN", "Polish Złoty", "波兰兹罗提", "zł", "PL", Region.Europe),
        c("RON", "Romanian Leu", "罗马尼亚列伊", "lei", "RO", Region.Europe),
        c("RSD", "Serbian Dinar", "塞尔维亚第纳尔", "дин", "RS", Region.Europe),
        c("RUB", "Russian Ruble", "俄罗斯卢布", "₽", "RU", Region.Europe),
        c("SEK", "Swedish Krona", "瑞典克朗", "kr", "SE", Region.Europe),
        c("TRY", "Turkish Lira", "土耳其里拉", "₺", "TR", Region.Europe),
        c("UAH", "Ukrainian Hryvnia", "乌克兰格里夫纳", "₴", "UA", Region.Europe),

        // ── Americas ──────────────────────────────────────────────────────────
        c("ANG", "Netherlands Antillean Guilder", "荷属安的列斯盾", "ƒ", "CW", Region.Americas),
        c("ARS", "Argentine Peso", "阿根廷比索", "AR$", "AR", Region.Americas),
        c("AWG", "Aruban Florin", "阿鲁巴弗罗林", "ƒ", "AW", Region.Americas),
        c("BBD", "Barbadian Dollar", "巴巴多斯元", "Bds$", "BB", Region.Americas),
        c("BMD", "Bermudian Dollar", "百慕大元", "BD$", "BM", Region.Americas),
        c("BOB", "Bolivian Boliviano", "玻利维亚诺", "Bs", "BO", Region.Americas),
        c("BRL", "Brazilian Real", "巴西雷亚尔", "R$", "BR", Region.Americas),
        c("BSD", "Bahamian Dollar", "巴哈马元", "B$", "BS", Region.Americas),
        c("BZD", "Belize Dollar", "伯利兹元", "BZ$", "BZ", Region.Americas),
        c("CAD", "Canadian Dollar", "加元", "C$", "CA", Region.Americas),
        c("CLF", "Chilean Unit of Account", "智利记账单位", "UF", "CL", Region.Americas, 4),
        c("CLP", "Chilean Peso", "智利比索", "CL$", "CL", Region.Americas, 0),
        c("COP", "Colombian Peso", "哥伦比亚比索", "CO$", "CO", Region.Americas),
        c("CRC", "Costa Rican Colón", "哥斯达黎加科朗", "₡", "CR", Region.Americas),
        c("CUP", "Cuban Peso", "古巴比索", "₱", "CU", Region.Americas),
        c("DOP", "Dominican Peso", "多米尼加比索", "RD$", "DO", Region.Americas),
        c("FKP", "Falkland Islands Pound", "福克兰群岛镑", "£", "FK", Region.Americas),
        c("GTQ", "Guatemalan Quetzal", "危地马拉格查尔", "Q", "GT", Region.Americas),
        c("GYD", "Guyanaese Dollar", "圭亚那元", "G$", "GY", Region.Americas),
        c("HNL", "Honduran Lempira", "洪都拉斯伦皮拉", "L", "HN", Region.Americas),
        c("HTG", "Haitian Gourde", "海地古德", "G", "HT", Region.Americas),
        c("JMD", "Jamaican Dollar", "牙买加元", "J$", "JM", Region.Americas),
        c("KYD", "Cayman Islands Dollar", "开曼群岛元", "CI$", "KY", Region.Americas),
        c("MXN", "Mexican Peso", "墨西哥比索", "MX$", "MX", Region.Americas),
        c("NIO", "Nicaraguan Córdoba", "尼加拉瓜科多巴", "C$", "NI", Region.Americas),
        c("PAB", "Panamanian Balboa", "巴拿马巴波亚", "B/.", "PA", Region.Americas),
        c("PEN", "Peruvian Sol", "秘鲁索尔", "S/", "PE", Region.Americas),
        c("PYG", "Paraguayan Guaraní", "巴拉圭瓜拉尼", "₲", "PY", Region.Americas, 0),
        c("SRD", "Surinamese Dollar", "苏里南元", "S$", "SR", Region.Americas),
        c("TTD", "Trinidad & Tobago Dollar", "特立尼达和多巴哥元", "TT$", "TT", Region.Americas),
        c("USD", "US Dollar", "美元", "$", "US", Region.Americas),
        c("UYU", "Uruguayan Peso", "乌拉圭比索", "\$U", "UY", Region.Americas),
        c("VES", "Venezuelan Bolívar", "委内瑞拉玻利瓦尔", "Bs.S", "VE", Region.Americas),
        c("XCD", "East Caribbean Dollar", "东加勒比元", "EC$", "AG", Region.Americas),
        c("XCG", "Caribbean Guilder", "加勒比盾", "Cg", "CW", Region.Americas),

        // ── Middle East ───────────────────────────────────────────────────────
        c("AED", "UAE Dirham", "阿联酋迪拉姆", "د.إ", "AE", Region.MiddleEast),
        c("BHD", "Bahraini Dinar", "巴林第纳尔", ".د.ب", "BH", Region.MiddleEast, 3),
        c("ILS", "Israeli New Shekel", "以色列新谢克尔", "₪", "IL", Region.MiddleEast),
        c("IQD", "Iraqi Dinar", "伊拉克第纳尔", "ع.د", "IQ", Region.MiddleEast, 3),
        c("IRR", "Iranian Rial", "伊朗里亚尔", "﷼", "IR", Region.MiddleEast),
        c("JOD", "Jordanian Dinar", "约旦第纳尔", "د.ا", "JO", Region.MiddleEast, 3),
        c("KWD", "Kuwaiti Dinar", "科威特第纳尔", "د.ك", "KW", Region.MiddleEast, 3),
        c("LBP", "Lebanese Pound", "黎巴嫩镑", "ل.ل", "LB", Region.MiddleEast),
        c("OMR", "Omani Rial", "阿曼里亚尔", "﷼", "OM", Region.MiddleEast, 3),
        c("QAR", "Qatari Riyal", "卡塔尔里亚尔", "﷼", "QA", Region.MiddleEast),
        c("SAR", "Saudi Riyal", "沙特里亚尔", "﷼", "SA", Region.MiddleEast),
        c("SYP", "Syrian Pound", "叙利亚镑", "£S", "SY", Region.MiddleEast),
        c("YER", "Yemeni Rial", "也门里亚尔", "﷼", "YE", Region.MiddleEast),

        // ── Africa ────────────────────────────────────────────────────────────
        c("AOA", "Angolan Kwanza", "安哥拉宽扎", "Kz", "AO", Region.Africa),
        c("BIF", "Burundian Franc", "布隆迪法郎", "FBu", "BI", Region.Africa, 0),
        c("BWP", "Botswanan Pula", "博茨瓦纳普拉", "P", "BW", Region.Africa),
        c("CDF", "Congolese Franc", "刚果法郎", "FC", "CD", Region.Africa),
        c("CVE", "Cape Verdean Escudo", "佛得角埃斯库多", "$", "CV", Region.Africa),
        c("DJF", "Djiboutian Franc", "吉布提法郎", "Fdj", "DJ", Region.Africa, 0),
        c("DZD", "Algerian Dinar", "阿尔及利亚第纳尔", "د.ج", "DZ", Region.Africa),
        c("EGP", "Egyptian Pound", "埃及镑", "E£", "EG", Region.Africa),
        c("ERN", "Eritrean Nakfa", "厄立特里亚纳克法", "Nfk", "ER", Region.Africa),
        c("ETB", "Ethiopian Birr", "埃塞俄比亚比尔", "Br", "ET", Region.Africa),
        c("GHS", "Ghanaian Cedi", "加纳塞地", "GH₵", "GH", Region.Africa),
        c("GMD", "Gambian Dalasi", "冈比亚达拉西", "D", "GM", Region.Africa),
        c("GNF", "Guinean Franc", "几内亚法郎", "FG", "GN", Region.Africa, 0),
        c("KES", "Kenyan Shilling", "肯尼亚先令", "KSh", "KE", Region.Africa),
        c("KMF", "Comorian Franc", "科摩罗法郎", "CF", "KM", Region.Africa, 0),
        c("LRD", "Liberian Dollar", "利比里亚元", "L$", "LR", Region.Africa),
        c("LSL", "Lesotho Loti", "莱索托洛提", "L", "LS", Region.Africa),
        c("LYD", "Libyan Dinar", "利比亚第纳尔", "ل.د", "LY", Region.Africa, 3),
        c("MAD", "Moroccan Dirham", "摩洛哥迪拉姆", "د.م.", "MA", Region.Africa),
        c("MGA", "Malagasy Ariary", "马达加斯加阿里亚里", "Ar", "MG", Region.Africa),
        c("MRU", "Mauritanian Ouguiya", "毛里塔尼亚乌吉亚", "UM", "MR", Region.Africa),
        c("MUR", "Mauritian Rupee", "毛里求斯卢比", "₨", "MU", Region.Africa),
        c("MWK", "Malawian Kwacha", "马拉维克瓦查", "MK", "MW", Region.Africa),
        c("MZN", "Mozambican Metical", "莫桑比克梅蒂卡尔", "MT", "MZ", Region.Africa),
        c("NAD", "Namibian Dollar", "纳米比亚元", "N$", "NA", Region.Africa),
        c("NGN", "Nigerian Naira", "尼日利亚奈拉", "₦", "NG", Region.Africa),
        c("RWF", "Rwandan Franc", "卢旺达法郎", "FRw", "RW", Region.Africa, 0),
        c("SCR", "Seychellois Rupee", "塞舌尔卢比", "₨", "SC", Region.Africa),
        c("SDG", "Sudanese Pound", "苏丹镑", "ج.س.", "SD", Region.Africa),
        c("SHP", "Saint Helena Pound", "圣赫勒拿镑", "£", "SH", Region.Africa),
        c("SLE", "Sierra Leonean Leone", "塞拉利昂利昂", "Le", "SL", Region.Africa),
        c("SLL", "Sierra Leonean Leone (old)", "塞拉利昂利昂（旧）", "Le", "SL", Region.Africa),
        c("SOS", "Somali Shilling", "索马里先令", "Sh", "SO", Region.Africa),
        c("SSP", "South Sudanese Pound", "南苏丹镑", "£", "SS", Region.Africa),
        c("STN", "São Tomé & Príncipe Dobra", "圣多美和普林西比多布拉", "Db", "ST", Region.Africa),
        c("SZL", "Swazi Lilangeni", "斯威士兰里兰吉尼", "L", "SZ", Region.Africa),
        c("TND", "Tunisian Dinar", "突尼斯第纳尔", "د.ت", "TN", Region.Africa, 3),
        c("TZS", "Tanzanian Shilling", "坦桑尼亚先令", "TSh", "TZ", Region.Africa),
        c("UGX", "Ugandan Shilling", "乌干达先令", "USh", "UG", Region.Africa, 0),
        c("XAF", "Central African CFA Franc", "中非法郎", "FCFA", "CM", Region.Africa, 0),
        c("XOF", "West African CFA Franc", "西非法郎", "CFA", "SN", Region.Africa, 0),
        c("ZAR", "South African Rand", "南非兰特", "R", "ZA", Region.Africa),
        c("ZMW", "Zambian Kwacha", "赞比亚克瓦查", "ZK", "ZM", Region.Africa),
        c("ZWG", "Zimbabwean Gold", "津巴布韦黄金本位币", "ZiG", "ZW", Region.Africa),
        c("ZWL", "Zimbabwean Dollar", "津巴布韦元", "Z$", "ZW", Region.Africa),

        // ── Oceania ───────────────────────────────────────────────────────────
        c("AUD", "Australian Dollar", "澳元", "A$", "AU", Region.Oceania),
        c("FJD", "Fijian Dollar", "斐济元", "FJ$", "FJ", Region.Oceania),
        c("KID", "Kiribati Dollar", "基里巴斯元", "$", "KI", Region.Oceania),
        c("NZD", "New Zealand Dollar", "新西兰元", "NZ$", "NZ", Region.Oceania),
        c("PGK", "Papua New Guinean Kina", "巴新基那", "K", "PG", Region.Oceania),
        c("SBD", "Solomon Islands Dollar", "所罗门群岛元", "SI$", "SB", Region.Oceania),
        c("TOP", "Tongan Paʻanga", "汤加潘加", "T$", "TO", Region.Oceania),
        c("TVD", "Tuvaluan Dollar", "图瓦卢元", "$", "TV", Region.Oceania),
        c("VUV", "Vanuatu Vatu", "瓦努阿图瓦图", "VT", "VU", Region.Oceania, 0),
        c("WST", "Samoan Tālā", "萨摩亚塔拉", "WS$", "WS", Region.Oceania),
        c("XPF", "CFP Franc", "太平洋法郎", "₣", "PF", Region.Oceania, 0),

        // ── Precious metals (per troy ounce) ──────────────────────────────────
        c("XAU", "Gold", "黄金", "Au", "XX", Region.Metal, 6),
        c("XAG", "Silver", "白银", "Ag", "XX", Region.Metal, 6),
        c("XPT", "Platinum", "铂金", "Pt", "XX", Region.Metal, 6),
        c("XPD", "Palladium", "钯金", "Pd", "XX", Region.Metal, 6),

        // ── Digital assets ────────────────────────────────────────────────────
        c("BTC", "Bitcoin", "比特币", "₿", "XX", Region.Crypto, 8),
        c("ETH", "Ethereum", "以太坊", "Ξ", "XX", Region.Crypto, 8),
        c("USDT", "Tether", "泰达币", "₮", "XX", Region.Crypto, 4),
        c("USDC", "USD Coin", "USD Coin", "$", "XX", Region.Crypto, 4),
        c("XRP", "XRP", "瑞波币", "XRP", "XX", Region.Crypto, 6),
        c("SOL", "Solana", "索拉纳", "SOL", "XX", Region.Crypto, 6),
        c("DOGE", "Dogecoin", "狗狗币", "Ð", "XX", Region.Crypto, 6),
        c("ADA", "Cardano", "艾达币", "ADA", "XX", Region.Crypto, 6),
        c("TRX", "TRON", "波场", "TRX", "XX", Region.Crypto, 6),
        c("LINK", "Chainlink", "Chainlink", "LINK", "XX", Region.Crypto, 6),
        c("AVAX", "Avalanche", "雪崩", "AVAX", "XX", Region.Crypto, 6),
        c("DOT", "Polkadot", "波卡", "DOT", "XX", Region.Crypto, 6),
        c("BCH", "Bitcoin Cash", "比特币现金", "BCH", "XX", Region.Crypto, 8),
        c("LTC", "Litecoin", "莱特币", "Ł", "XX", Region.Crypto, 8),
        c("XLM", "Stellar Lumens", "恒星币", "XLM", "XX", Region.Crypto, 6),
        c("ATOM", "Cosmos", "Cosmos", "ATOM", "XX", Region.Crypto, 6),
        c("UNI", "Uniswap", "Uniswap", "UNI", "XX", Region.Crypto, 6),
        c("ETC", "Ethereum Classic", "以太经典", "ETC", "XX", Region.Crypto, 6),
        c("FIL", "Filecoin", "Filecoin", "FIL", "XX", Region.Crypto, 6),
        c("AAVE", "Aave", "Aave", "AAVE", "XX", Region.Crypto, 6),

        // ── Other ─────────────────────────────────────────────────────────────
        c("XDR", "IMF Special Drawing Rights", "特别提款权", "SDR", "XX", Region.Other, 4),
    )

    private val byCode: Map<String, Currency> = all.associateBy { it.code }

    /** Codes shown on first launch, ordered by how often people convert them. */
    val defaultFavorites: List<String> =
        listOf("USD", "CNY", "EUR", "JPY", "GBP", "HKD", "KRW", "AUD")

    operator fun get(code: String): Currency? = byCode[code]

    /**
     * Metadata for an arbitrary code. Codes the API adds later are still usable —
     * they render with the raw code, no flag, and land in [Region.Other].
     */
    fun resolve(code: String): Currency =
        byCode[code] ?: Currency(
            code = code,
            name = code,
            nameZh = code,
            symbol = code,
            flag = "XX",
            region = Region.Other,
            searchKey = code.lowercase(),
        )

    /** Converts a region code to a regional-indicator flag emoji. */
    fun flagEmoji(region: String): String {
        if (region.length != 2 || region == "XX") return ""
        val base = 0x1F1E6 - 'A'.code
        val a = region[0].uppercaseChar()
        val b = region[1].uppercaseChar()
        if (a !in 'A'..'Z' || b !in 'A'..'Z') return ""
        return String(Character.toChars(base + a.code)) +
            String(Character.toChars(base + b.code))
    }
}
