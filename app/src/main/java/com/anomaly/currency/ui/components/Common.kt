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
package com.anomaly.currency.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.anomaly.currency.data.Currencies
import com.anomaly.currency.data.Currency
import com.anomaly.currency.ui.theme.AmountDisplayStyle

/**
 * A circular flag/initials avatar. Falls back to the first two letters of the
 * currency code when the region has no flag emoji (e.g. XAU, XDR).
 */
@Composable
fun CurrencyAvatar(
    currency: Currency,
    modifier: Modifier = Modifier,
    size: Int = 40,
) {
    val flag = Currencies.flagEmoji(currency.flag)
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        if (flag.isNotEmpty()) {
            Text(
                text = flag,
                style = MaterialTheme.typography.titleLarge,
            )
        } else {
            Text(
                text = currency.code.take(2),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

/**
 * The tappable amount row used for both sides of the pair.
 *
 * [focused] drives an emphasized container plus a larger type ramp, which is how
 * the user knows which side they are editing. Contrast is carried by
 * primaryContainer/onPrimaryContainer rather than by a border, so the emphasis
 * survives both light and dark schemes.
 */
@Composable
fun AmountRow(
    currency: Currency,
    amount: String,
    focused: Boolean,
    onClick: () -> Unit,
    onPickCurrency: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val container = if (focused) scheme.primaryContainer else scheme.surfaceContainerHigh
    val onContainer = if (focused) scheme.onPrimaryContainer else scheme.onSurface

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
        contentColor = onContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .clickable(onClick = onPickCurrency)
                    .padding(end = 8.dp)
                    .semantics {
                        contentDescription = "${currency.code} ${currency.nameZh}，点击更换货币"
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CurrencyAvatar(currency)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = currency.code,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = currency.nameZh,
                        style = MaterialTheme.typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            AnimatedAmount(
                text = amount,
                emphasized = focused,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Digit changes slide vertically like a mechanical counter — the Material
 * motion pattern for numeric transitions. Direction is inferred from length so
 * typing pushes up and deleting pulls down.
 */
@Composable
private fun AnimatedAmount(
    text: String,
    emphasized: Boolean,
    modifier: Modifier = Modifier,
) {
    val style = if (emphasized) {
        AmountDisplayStyle
    } else {
        MaterialTheme.typography.headlineMedium
    }
    AnimatedContent(
        targetState = text,
        modifier = modifier,
        transitionSpec = {
            val up = targetState.length >= initialState.length
            val h = if (up) 1 else -1
            (
                slideInVertically(tween(220)) { height -> h * height / 3 } +
                    fadeIn(tween(220))
                ) togetherWith (
                slideOutVertically(tween(180)) { height -> -h * height / 3 } +
                    fadeOut(tween(180))
                )
        },
        label = "amount",
    ) { value ->
        Text(
            text = value,
            style = style,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clearAndSetSemantics { contentDescription = value },
        )
    }
}

/** Thin divider that reads correctly against both light and dark surfaces. */
@Composable
fun HairLine(modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
