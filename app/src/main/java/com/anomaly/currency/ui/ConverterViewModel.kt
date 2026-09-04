package com.anomaly.currency.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anomaly.currency.data.Currencies
import com.anomaly.currency.data.Currency
import com.anomaly.currency.data.Provenance
import com.anomaly.currency.data.RateError
import com.anomaly.currency.data.RateRepository
import com.anomaly.currency.data.RateTable
import com.anomaly.currency.data.SettingsStore
import com.anomaly.currency.data.UserPreferences
import com.anomaly.currency.ui.theme.ThemeMode
import com.anomaly.currency.util.Money
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.math.BigDecimal

/** Which side of the pair the user is typing into. */
enum class ActiveField { From, To }

data class ConverterUiState(
    val from: Currency = Currencies.resolve("USD"),
    val to: Currency = Currencies.resolve("CNY"),
    val input: String = "100",
    val activeField: ActiveField = ActiveField.From,
    val favorites: List<String> = Currencies.defaultFavorites,
    val table: RateTable = RateTable.Empty,
    val loading: Boolean = false,
    val error: RateError? = null,
    val fromCache: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
) {
    /** Multiplier for the direction the user is currently editing. */
    val rate: Double?
        get() = when (activeField) {
            ActiveField.From -> table.rate(from.code, to.code)
            ActiveField.To -> table.rate(to.code, from.code)
        }

    /** Canonical from -> to rate, independent of which field is focused. */
    val forwardRate: Double? get() = table.rate(from.code, to.code)

    private val targetCurrency: Currency
        get() = if (activeField == ActiveField.From) to else from

    val amount: BigDecimal get() = Money.parse(input) ?: BigDecimal.ZERO

    /** Converted value shown in the non-focused field. */
    val result: String
        get() {
            val r = rate ?: return "—"
            return Money.format(
                Money.convert(amount, r, targetCurrency.decimals),
                targetCurrency.decimals,
            )
        }

    /** Whether the active pair is quoted at ECB benchmark grade. */
    val provenance: Provenance get() = table.provenanceOf(from.code, to.code)

    /** True before any rate table has been obtained. */
    val isBlank: Boolean get() = table.isEmpty
}

class ConverterViewModel(app: Application) : AndroidViewModel(app) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val settings = SettingsStore(app, json)
    private val repository = RateRepository(app, settings, json)

    private val editor = MutableStateFlow(EditorState())
    private val _state = MutableStateFlow(ConverterUiState())
    val state: StateFlow<ConverterUiState> = _state.asStateFlow()

    private data class EditorState(
        val input: String = "100",
        val activeField: ActiveField = ActiveField.From,
    )

    init {
        viewModelScope.launch {
            combine(
                settings.preferences,
                repository.state,
                editor,
            ) { prefs: UserPreferences, rates, ed ->
                ConverterUiState(
                    from = Currencies.resolve(prefs.from),
                    to = Currencies.resolve(prefs.to),
                    input = ed.input,
                    activeField = ed.activeField,
                    favorites = prefs.favorites,
                    table = rates.table,
                    loading = rates.loading,
                    error = rates.error,
                    fromCache = rates.fromCache,
                    themeMode = prefs.themeMode,
                    dynamicColor = prefs.dynamicColor,
                )
            }.collect { _state.value = it }
        }
        viewModelScope.launch {
            repository.primeFromLocal()
            repository.refresh()
        }
    }

    fun onInputChange(text: String) {
        val sanitized = sanitize(text)
        editor.value = editor.value.copy(input = sanitized)
    }

    fun onFieldFocused(field: ActiveField) {
        val current = editor.value
        if (current.activeField == field) return
        // Carry the currently displayed converted value into the newly focused
        // field so the number under the caret does not jump.
        val converted = _state.value.result.replace(",", "")
        editor.value = EditorState(
            input = if (converted == "—") current.input else converted,
            activeField = field,
        )
    }

    fun appendDigit(digit: Char) {
        val current = editor.value.input
        val next = when {
            digit == '.' && current.contains('.') -> current
            current == "0" && digit != '.' -> digit.toString()
            else -> current + digit
        }
        onInputChange(next)
    }

    fun backspace() {
        val current = editor.value.input
        editor.value = editor.value.copy(
            input = if (current.length <= 1) "0" else current.dropLast(1),
        )
    }

    fun clearInput() {
        editor.value = editor.value.copy(input = "0")
    }

    fun swap() {
        val s = _state.value
        viewModelScope.launch { settings.setPair(s.to.code, s.from.code) }
    }

    fun selectFrom(code: String) {
        val s = _state.value
        viewModelScope.launch {
            if (code == s.to.code) {
                settings.setPair(code, s.from.code)
            } else {
                settings.setPair(code, s.to.code)
            }
            rememberUsage(code)
        }
    }

    fun selectTo(code: String) {
        val s = _state.value
        viewModelScope.launch {
            if (code == s.from.code) {
                settings.setPair(s.to.code, code)
            } else {
                settings.setPair(s.from.code, code)
            }
            rememberUsage(code)
        }
    }

    fun toggleFavorite(code: String) {
        val current = _state.value.favorites
        val next = if (code in current) current - code else current + code
        viewModelScope.launch { settings.setFavorites(next) }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settings.setThemeMode(mode) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch { settings.setDynamicColor(enabled) }
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh(force = true) }
    }

    /** Moves a freshly picked currency to the front of the favorites strip. */
    private suspend fun rememberUsage(code: String) {
        val current = _state.value.favorites
        if (current.firstOrNull() == code) return
        val next = (listOf(code) + current.filterNot { it == code }).take(12)
        settings.setFavorites(next)
    }

    /** Keeps only digits and a single decimal separator. */
    private fun sanitize(text: String): String {
        val filtered = buildString(text.length) {
            var dotSeen = false
            for (ch in text) {
                when {
                    ch.isDigit() -> append(ch)
                    (ch == '.' || ch == ',') && !dotSeen -> {
                        dotSeen = true
                        append('.')
                    }
                }
            }
        }
        val normalized = when {
            filtered.isEmpty() -> "0"
            filtered.startsWith('.') -> "0$filtered"
            else -> filtered.trimStart('0').let { t ->
                when {
                    t.isEmpty() -> "0"
                    t.startsWith('.') -> "0$t"
                    else -> t
                }
            }
        }
        val intPart = normalized.substringBefore('.')
        if (intPart.length <= MAX_INT_DIGITS) return normalized
        // Clamp the integer part; absurd input must not break layout.
        val fraction = normalized.substringAfter('.', "")
        val clamped = intPart.take(MAX_INT_DIGITS)
        return if (fraction.isEmpty() && '.' !in normalized) clamped else "$clamped.$fraction"
    }

    companion object {
        private const val MAX_INT_DIGITS = 15

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: androidx.lifecycle.viewmodel.CreationExtras,
            ): T {
                val app = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as Application
                return ConverterViewModel(app) as T
            }
        }
    }
}
