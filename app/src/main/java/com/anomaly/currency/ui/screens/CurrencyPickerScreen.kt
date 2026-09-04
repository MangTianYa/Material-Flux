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
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anomaly.currency.data.Currencies
import com.anomaly.currency.data.Currency
import com.anomaly.currency.data.Provenance
import com.anomaly.currency.data.Region
import com.anomaly.currency.ui.components.AppIcons
import com.anomaly.currency.ui.components.CurrencyAvatar
import com.anomaly.currency.util.Money

/**
 * Full-screen currency picker.
 *
 * Layout zones, top to bottom:
 *  1. Search field pinned in the top bar, with a live result count.
 *  2. Horizontal region filter (全部 / 亚洲 / … / 贵金属 / 数字货币).
 *  3. Sticky region headers over the rows, with 常用 pinned first.
 *
 * Only codes present in the active rate table are offered — listing a currency
 * the app cannot convert would be a dead end.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerScreen(
    title: String,
    selectedCode: String,
    baseCode: String,
    availableCodes: Set<String>,
    favorites: List<String>,
    rateOf: (String) -> Double?,
    provenanceOf: (String) -> Provenance,
    onSelect: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var regionFilter by remember { mutableStateOf<Region?>(null) }
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    // Universe restricted to what the providers actually returned. Unknown codes
    // still surface so a newly added asset is usable before its metadata ships.
    val universe = remember(availableCodes) {
        if (availableCodes.isEmpty()) {
            Currencies.all
        } else {
            val known = Currencies.all.filter { it.code in availableCodes }
            val knownCodes = known.mapTo(HashSet()) { it.code }
            known + availableCodes.asSequence()
                .filterNot { it in knownCodes }
                .sorted()
                .map { Currencies.resolve(it) }
        }
    }

    val trimmedQuery = query.trim().lowercase()

    val matches by remember(universe, trimmedQuery, regionFilter) {
        derivedStateOf {
            universe.asSequence()
                .filter { regionFilter == null || it.region == regionFilter }
                .filter { trimmedQuery.isEmpty() || it.searchKey.contains(trimmedQuery) }
                .toList()
                .sortedWith(searchRanking(trimmedQuery))
        }
    }

    // 常用 is only meaningful while browsing; a search shows pure results.
    val pinned by remember(universe, favorites, trimmedQuery, regionFilter) {
        derivedStateOf {
            if (trimmedQuery.isNotEmpty() || regionFilter != null) {
                emptyList()
            } else {
                favorites.mapNotNull { code -> universe.firstOrNull { it.code == code } }
            }
        }
    }

    val grouped by remember(matches, trimmedQuery) {
        derivedStateOf {
            if (trimmedQuery.isNotEmpty()) {
                // Search results stay in relevance order, ungrouped.
                listOf<Pair<String?, List<Currency>>>(null to matches)
            } else {
                matches.groupBy { it.region }
                    .toSortedMap(compareBy { it.ordinal })
                    .map { (region, list) -> region.labelZh to list.sortedBy { it.code } }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(AppIcons.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
                SearchField(
                    query = query,
                    resultCount = matches.size,
                    totalCount = universe.size,
                    onQueryChange = { query = it },
                    onClear = { query = "" },
                    onSearch = { keyboard?.hide() },
                )
                RegionFilterRow(
                    selected = regionFilter,
                    visible = trimmedQuery.isEmpty(),
                    onSelect = { regionFilter = it },
                )
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            if (pinned.isNotEmpty()) {
                stickyHeader(key = "hdr-pinned") { RegionHeader("常用", pinned.size) }
                items(pinned, key = { "pin-${it.code}" }) { currency ->
                    CurrencyRow(
                        currency = currency,
                        selected = currency.code == selectedCode,
                        baseCode = baseCode,
                        rate = rateOf(currency.code),
                        provenance = provenanceOf(currency.code),
                        isFavorite = true,
                        searching = false,
                        onClick = { onSelect(currency.code) },
                        onToggleFavorite = { onToggleFavorite(currency.code) },
                    )
                }
            }

            if (matches.isEmpty()) {
                item(key = "empty") { EmptyState(query) }
            }

            for ((header, list) in grouped) {
                if (header != null) {
                    stickyHeader(key = "hdr-$header") { RegionHeader(header, list.size) }
                }
                items(list, key = { it.code }) { currency ->
                    CurrencyRow(
                        currency = currency,
                        selected = currency.code == selectedCode,
                        baseCode = baseCode,
                        rate = rateOf(currency.code),
                        provenance = provenanceOf(currency.code),
                        isFavorite = currency.code in favorites,
                        searching = trimmedQuery.isNotEmpty(),
                        onClick = { onSelect(currency.code) },
                        onToggleFavorite = { onToggleFavorite(currency.code) },
                    )
                }
            }
        }
    }
}

/**
 * Ranks results so an exact code match wins, then prefix matches, then
 * substring hits. Without this, searching "US" would bury USD under AUD.
 */
private fun searchRanking(query: String): Comparator<Currency> =
    if (query.isEmpty()) {
        compareBy { it.code }
    } else {
        compareBy<Currency> { c ->
            when {
                c.code.equals(query, ignoreCase = true) -> 0
                c.code.lowercase().startsWith(query) -> 1
                c.nameZh.startsWith(query) -> 2
                c.name.lowercase().startsWith(query) -> 3
                c.nameZh.contains(query) -> 4
                else -> 5
            }
        }.thenBy { it.code }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchField(
    query: String,
    resultCount: Int,
    totalCount: Int,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: () -> Unit,
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        placeholder = { Text("搜索 $totalCount 种货币 · 代码或名称") },
        leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
        trailingIcon = {
            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(tween(120)),
                exit = fadeOut(tween(120)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$resultCount",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    IconButton(onClick = onClear) {
                        Icon(AppIcons.Close, contentDescription = "清除搜索")
                    }
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegionFilterRow(
    selected: Region?,
    visible: Boolean,
    onSelect: (Region?) -> Unit,
) {
    // Hidden during search: mixing a region filter into ranked results makes the
    // result count misleading.
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(120)),
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                FilterChip(
                    selected = selected == null,
                    onClick = { onSelect(null) },
                    label = { Text("全部") },
                    shape = MaterialTheme.shapes.small,
                )
            }
            items(Region.entries.toList(), key = { it.name }) { region ->
                FilterChip(
                    selected = selected == region,
                    onClick = { onSelect(if (selected == region) null else region) },
                    label = { Text(region.labelZh) },
                    shape = MaterialTheme.shapes.small,
                )
            }
        }
    }
}

@Composable
private fun RegionHeader(label: String, count: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyState(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, start = 32.dp, end = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            AppIcons.Search,
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = "找不到“$query”",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "可以试试代码（USD、BTC）、中文名（美元、黄金）或英文名（Dollar）",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CurrencyRow(
    currency: Currency,
    selected: Boolean,
    baseCode: String,
    rate: Double?,
    provenance: Provenance,
    isFavorite: Boolean,
    searching: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val container by animateColorAsState(
        targetValue = if (selected) scheme.secondaryContainer else Color.Transparent,
        animationSpec = tween(180),
        label = "rowContainer",
    )
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 2.dp)
                .background(container, MaterialTheme.shapes.large)
                .padding(start = 12.dp, end = 4.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CurrencyAvatar(currency)
            Spacer(Modifier.width(14.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency.code,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        color = if (selected) scheme.onSecondaryContainer else scheme.onSurface,
                    )
                    if (currency.symbol != currency.code) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = currency.symbol,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
                Text(
                    text = currency.nameZh,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // Surfacing the English name only while searching keeps the
                // browse list to two lines but still explains why a row matched.
                if (searching && currency.name != currency.nameZh) {
                    Text(
                        text = currency.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (rate != null && currency.code != baseCode) {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 4.dp),
                ) {
                    Text(
                        text = Money.formatRate(rate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = when (provenance) {
                            Provenance.Ecb -> "ECB · 1 $baseCode"
                            Provenance.Metal -> "现货 · 1 $baseCode"
                            Provenance.Crypto -> "实时 · 1 $baseCode"
                            Provenance.Aggregate -> "1 $baseCode"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) AppIcons.Star else AppIcons.StarOutline,
                    contentDescription = if (isFavorite) "取消常用" else "设为常用",
                    tint = if (isFavorite) scheme.primary else scheme.onSurfaceVariant,
                )
            }
        }
    }
}
