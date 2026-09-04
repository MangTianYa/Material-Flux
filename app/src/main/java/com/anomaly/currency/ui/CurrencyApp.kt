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
package com.anomaly.currency.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.anomaly.currency.ui.screens.AppearanceSheet
import com.anomaly.currency.ui.screens.ConverterScreen
import com.anomaly.currency.ui.screens.CurrencyPickerScreen

/** Top-level destinations. Kept as a sealed type instead of pulling in Navigation-Compose. */
private sealed interface Destination {
    data object Converter : Destination
    data class Picker(val target: ActiveField) : Destination
}

@Composable
fun CurrencyApp(
    state: ConverterUiState,
    viewModel: ConverterViewModel,
) {
    var destination by remember { mutableStateOf<Destination>(Destination.Converter) }
    var showAppearance by remember { mutableStateOf(false) }

    BackHandler(enabled = destination !is Destination.Converter) {
        destination = Destination.Converter
    }

    AnimatedContent(
        targetState = destination,
        transitionSpec = {
            val forward = targetState is Destination.Picker
            val dir = if (forward) 1 else -1
            (
                slideInHorizontally(tween(280)) { w -> dir * w / 6 } + fadeIn(tween(200))
                ) togetherWith (
                slideOutHorizontally(tween(240)) { w -> -dir * w / 8 } + fadeOut(tween(160))
                )
        },
        label = "destination",
    ) { current ->
        when (current) {
            Destination.Converter -> ConverterScreen(
                state = state,
                onInputDigit = viewModel::appendDigit,
                onBackspace = viewModel::backspace,
                onClear = viewModel::clearInput,
                onFocusField = viewModel::onFieldFocused,
                onSwap = viewModel::swap,
                onPickFrom = { destination = Destination.Picker(ActiveField.From) },
                onPickTo = { destination = Destination.Picker(ActiveField.To) },
                onSelectFavorite = { code ->
                    when (state.activeField) {
                        ActiveField.From -> viewModel.selectFrom(code)
                        ActiveField.To -> viewModel.selectTo(code)
                    }
                },
                onRefresh = viewModel::refresh,
                onOpenSettings = { showAppearance = true },
            )

            is Destination.Picker -> {
                val isFrom = current.target == ActiveField.From
                val selected = if (isFrom) state.from.code else state.to.code
                val base = if (isFrom) state.to.code else state.from.code
                CurrencyPickerScreen(
                    title = if (isFrom) "选择源货币" else "选择目标货币",
                    selectedCode = selected,
                    baseCode = base,
                    availableCodes = state.table.rates.keys,
                    favorites = state.favorites,
                    rateOf = { code -> state.table.rate(base, code) },
                    provenanceOf = { code -> state.table.provenanceOf(code) },
                    onSelect = { code ->
                        if (isFrom) viewModel.selectFrom(code) else viewModel.selectTo(code)
                        destination = Destination.Converter
                    },
                    onToggleFavorite = viewModel::toggleFavorite,
                    onBack = { destination = Destination.Converter },
                )
            }
        }
    }

    if (showAppearance) {
        AppearanceSheet(
            themeMode = state.themeMode,
            dynamicColor = state.dynamicColor,
            onThemeModeChange = viewModel::setThemeMode,
            onDynamicColorChange = viewModel::setDynamicColor,
            onDismiss = { showAppearance = false },
        )
    }
}
