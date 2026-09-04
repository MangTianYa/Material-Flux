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
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/**
 * Single source of truth for rates.
 *
 * Start-up order: cached table (instant, possibly stale) then a network refresh.
 * There is no bundled fallback — a hard-coded snapshot would silently serve
 * month-old numbers, which is worse than telling the user to reconnect.
 */
class RateRepository(
    private val context: Context,
    private val settings: SettingsStore,
    json: Json,
) {

    private val api = RateApi(json)
    private val refreshLock = Mutex()

    private val _state = MutableStateFlow(RateState())
    val state: StateFlow<RateState> = _state.asStateFlow()

    /** Loads the cached table so the first frame has numbers. */
    suspend fun primeFromLocal() {
        if (!_state.value.table.isEmpty) return
        val cached = runCatching { settings.cached() }.getOrNull() ?: return
        _state.value = _state.value.copy(table = cached, fromCache = true)
    }

    /**
     * Fetches a fresh table. [force] bypasses the freshness window.
     * Never throws; failures surface through [RateState.error].
     */
    suspend fun refresh(force: Boolean = false) {
        if (refreshLock.isLocked) return
        refreshLock.withLock {
            val current = _state.value
            val fresh = !current.fromCache &&
                !current.table.isEmpty &&
                System.currentTimeMillis() - current.fetchedAtMillis < FRESH_WINDOW_MS
            if (fresh && !force) return

            if (!isOnline()) {
                _state.value = current.copy(loading = false, error = RateError.Offline)
                return
            }

            _state.value = current.copy(loading = true, error = null)

            _state.value = runCatching { api.latest() }.fold(
                onSuccess = { table ->
                    runCatching { settings.cache(table) }
                    RateState(
                        table = table,
                        loading = false,
                        error = null,
                        fromCache = false,
                        fetchedAtMillis = System.currentTimeMillis(),
                    )
                },
                onFailure = {
                    _state.value.copy(loading = false, error = RateError.Failed)
                },
            )
        }
    }

    /** True when a network is available; assumes online if the service is absent. */
    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private companion object {
        const val FRESH_WINDOW_MS = 30 * 60 * 1000L
    }
}

enum class RateError { Offline, Failed }

data class RateState(
    val table: RateTable = RateTable.Empty,
    val loading: Boolean = false,
    val error: RateError? = null,
    /** True while showing a persisted table that has not been re-verified. */
    val fromCache: Boolean = false,
    val fetchedAtMillis: Long = 0L,
) {
    /** Nothing to convert with yet — the UI shows a first-run loading screen. */
    val isBlank: Boolean get() = table.isEmpty
}
