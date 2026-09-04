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
package com.anomaly.currency.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anomaly.currency.data.Currencies
import com.anomaly.currency.data.Provenance
import com.anomaly.currency.data.RateError
import com.anomaly.currency.ui.ActiveField
import com.anomaly.currency.ui.ConverterUiState
import com.anomaly.currency.ui.components.AmountRow
import com.anomaly.currency.ui.components.AppIcons
import com.anomaly.currency.ui.components.CurrencyAvatar
import com.anomaly.currency.ui.components.NumericKeypad
import com.anomaly.currency.util.Money
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    state: ConverterUiState,
    onInputDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onFocusField: (ActiveField) -> Unit,
    onSwap: () -> Unit,
    onPickFrom: () -> Unit,
    onPickTo: () -> Unit,
    onSelectFavorite: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { Text("汇率兑换") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                actions = {
                    IconButton(onClick = onRefresh, enabled = !state.loading) {
                        if (state.loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(AppIcons.Refresh, contentDescription = "刷新汇率")
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(AppIcons.Palette, contentDescription = "外观设置")
                    }
                },
            )
        },
    ) { padding ->
        if (state.isBlank) {
            FirstRunState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                loading = state.loading,
                error = state.error,
                onRetry = onRefresh,
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            StatusStrip(state = state)
            Spacer(Modifier.height(8.dp))

            PairCard(
                state = state,
                onFocusField = onFocusField,
                onSwap = onSwap,
                onPickFrom = onPickFrom,
                onPickTo = onPickTo,
            )

            Spacer(Modifier.height(16.dp))
            FavoritesStrip(state = state, onSelect = onSelectFavorite)
            Spacer(Modifier.height(16.dp))

            NumericKeypad(
                onDigit = onInputDigit,
                onBackspace = onBackspace,
                onClear = onClear,
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

/**
 * Shown before any rate table exists. There is no bundled fallback, so this is
 * the honest state: either we are fetching, or we cannot and the user must act.
 */
@Composable
private fun FirstRunState(
    loading: Boolean,
    error: RateError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(20.dp))
            Text(
                text = "正在获取汇率",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "数据来自欧洲央行每日参考汇率",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        } else {
            Icon(
                AppIcons.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                text = if (error == RateError.Offline) "当前无网络连接" else "汇率获取失败",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "本应用只使用实时汇率，不提供内置的过期数据。请连接网络后重试。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text("重试") }
        }
    }
}

/** Cache / offline / failure banner. Collapses entirely when rates are live. */
@Composable
private fun StatusStrip(state: ConverterUiState) {
    val scheme = MaterialTheme.colorScheme
    val message = when {
        state.error == RateError.Offline -> "离线 · 显示上次同步的汇率"
        state.error == RateError.Failed -> "更新失败 · 显示上次同步的汇率"
        state.fromCache -> "正在校验汇率…"
        else -> null
    }
    AnimatedVisibility(
        visible = message != null,
        enter = expandVertically(tween(240)) + fadeIn(tween(240)),
        exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = scheme.tertiaryContainer,
            contentColor = scheme.onTertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    AppIcons.CloudOff,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                )
            }
        }
    }
}

@Composable
private fun PairCard(
    state: ConverterUiState,
    onFocusField: (ActiveField) -> Unit,
    onSwap: () -> Unit,
    onPickFrom: () -> Unit,
    onPickTo: () -> Unit,
) {
    val fromText = if (state.activeField == ActiveField.From) {
        formatEditing(state)
    } else {
        state.result
    }
    val toText = if (state.activeField == ActiveField.To) {
        formatEditing(state)
    } else {
        state.result
    }

    Box(Modifier.fillMaxWidth()) {
        Column {
            AmountRow(
                currency = state.from,
                amount = fromText,
                focused = state.activeField == ActiveField.From,
                onClick = { onFocusField(ActiveField.From) },
                onPickCurrency = onPickFrom,
            )
            Spacer(Modifier.height(10.dp))
            AmountRow(
                currency = state.to,
                amount = toText,
                focused = state.activeField == ActiveField.To,
                onClick = { onFocusField(ActiveField.To) },
                onPickCurrency = onPickTo,
            )
        }

        FilledIconButton(
            onClick = onSwap,
            modifier = Modifier
                .align(Alignment.Center)
                .size(48.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
        ) {
            Icon(AppIcons.SwapVert, contentDescription = "交换货币")
        }
    }

    Spacer(Modifier.height(14.dp))
    RateLine(state)
}

/** Groups the raw editor string so the focused field also reads as money. */
private fun formatEditing(state: ConverterUiState): String {
    val raw = state.input
    val intPart = raw.substringBefore('.')
    val hasDot = '.' in raw
    val fraction = raw.substringAfter('.', "")
    val grouped = intPart.reversed().chunked(3).joinToString(",").reversed()
    return buildString {
        append(if (grouped.isEmpty()) "0" else grouped)
        if (hasDot) {
            append('.')
            append(fraction)
        }
    }
}

/**
 * Rate line, six decimals both directions, plus a provenance badge so the user
 * knows whether this pair is an ECB benchmark quote or an aggregate estimate.
 */
@Composable
private fun RateLine(state: ConverterUiState) {
    val rate = state.forwardRate
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.padding(horizontal = 6.dp)) {
        if (rate == null) {
            Text(
                text = "该货币对暂无汇率",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
            )
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "1 ${state.from.code} = ${Money.formatRate(rate)} ${state.to.code}",
                style = MaterialTheme.typography.bodyLarge,
                color = scheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            ProvenanceBadge(state.provenance)
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "1 ${state.to.code} = ${Money.formatRate(1.0 / rate)} ${state.from.code}",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(6.dp))

        val detail = buildString {
            append(
                when (state.provenance) {
                    Provenance.Ecb -> "欧洲央行参考汇率"
                    Provenance.Metal -> "贵金属现货价 · 每金衡盎司"
                    Provenance.Crypto -> "数字资产实时价"
                    Provenance.Aggregate -> "综合市场汇率"
                },
            )
            if (state.provenance == Provenance.Ecb && state.table.ecbDate.isNotEmpty()) {
                append(" · ")
                append(state.table.ecbDate)
            } else if (state.table.updatedAtMillis > 0) {
                append(" · ")
                append(
                    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                        .format(Date(state.table.updatedAtMillis)),
                )
            }
        }
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
        )
    }
}

@Composable
private fun ProvenanceBadge(provenance: Provenance) {
    val scheme = MaterialTheme.colorScheme
    val (label, container, content) = when (provenance) {
        Provenance.Ecb ->
            Triple("ECB 基准", scheme.primaryContainer, scheme.onPrimaryContainer)
        Provenance.Metal ->
            Triple("贵金属现货", scheme.tertiaryContainer, scheme.onTertiaryContainer)
        Provenance.Crypto ->
            Triple("数字资产", scheme.tertiaryContainer, scheme.onTertiaryContainer)
        Provenance.Aggregate ->
            Triple("综合报价", scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = container,
        contentColor = content,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoritesStrip(
    state: ConverterUiState,
    onSelect: (String) -> Unit,
) {
    Column {
        Text(
            text = "快速切换",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.favorites, key = { it }) { code ->
                val currency = Currencies.resolve(code)
                val selected = code == state.to.code || code == state.from.code
                FilterChip(
                    selected = selected,
                    onClick = { onSelect(code) },
                    label = { Text(code, maxLines = 1, overflow = TextOverflow.Clip) },
                    leadingIcon = { CurrencyAvatar(currency = currency, size = 22) },
                )
            }
        }
    }
}
