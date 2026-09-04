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

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anomaly.currency.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_prefs")

/** Persisted user preferences plus the last known rate table. */
class SettingsStore(context: Context, private val json: Json) {

    private val store = context.dataStore

    private object Keys {
        val themeMode = stringPreferencesKey("theme_mode")
        val dynamicColor = booleanPreferencesKey("dynamic_color")
        val from = stringPreferencesKey("from_code")
        val to = stringPreferencesKey("to_code")
        val favorites = stringPreferencesKey("favorites")
        val cachedRates = stringPreferencesKey("cached_rates")
        val cachedAt = longPreferencesKey("cached_at")
        val cachedEcbDate = stringPreferencesKey("cached_ecb_date")
        val cachedEcbCodes = stringPreferencesKey("cached_ecb_codes")
    }

    val preferences: Flow<UserPreferences> = store.data.map { p ->
        UserPreferences(
            themeMode = ThemeMode.fromKey(p[Keys.themeMode]),
            dynamicColor = p[Keys.dynamicColor] ?: true,
            from = p[Keys.from] ?: "USD",
            to = p[Keys.to] ?: "CNY",
            favorites = p[Keys.favorites]
                ?.split(',')
                ?.filter { it.isNotBlank() }
                ?: Currencies.defaultFavorites,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[Keys.themeMode] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        store.edit { it[Keys.dynamicColor] = enabled }
    }

    suspend fun setPair(from: String, to: String) {
        store.edit {
            it[Keys.from] = from
            it[Keys.to] = to
        }
    }

    suspend fun setFavorites(codes: List<String>) {
        store.edit { it[Keys.favorites] = codes.joinToString(",") }
    }

    suspend fun cache(table: RateTable) {
        store.edit {
            it[Keys.cachedRates] = json.encodeToString(table.rates)
            it[Keys.cachedAt] = table.updatedAtMillis
            it[Keys.cachedEcbDate] = table.ecbDate
            it[Keys.cachedEcbCodes] = table.ecbCodes.joinToString(",")
        }
    }

    /** Last successfully fetched table, or null when nothing was stored yet. */
    suspend fun cached(): RateTable? {
        val p = store.data.first()
        val raw = p[Keys.cachedRates] ?: return null
        val rates = runCatching {
            json.decodeFromString<Map<String, Double>>(raw)
        }.getOrNull() ?: return null
        if (rates.isEmpty()) return null
        return RateTable(
            rates = rates,
            updatedAtMillis = p[Keys.cachedAt] ?: 0L,
            ecbDate = p[Keys.cachedEcbDate].orEmpty(),
            ecbCodes = p[Keys.cachedEcbCodes]
                ?.split(',')
                ?.filter { it.isNotBlank() }
                ?.toSet()
                ?: emptySet(),
        )
    }
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val from: String = "USD",
    val to: String = "CNY",
    val favorites: List<String> = Currencies.defaultFavorites,
)
