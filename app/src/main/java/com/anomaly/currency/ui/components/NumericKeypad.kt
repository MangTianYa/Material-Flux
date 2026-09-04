package com.anomaly.currency.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * In-app numeric keypad. Using a custom keypad instead of the IME keeps the
 * layout stable (no window resize on focus) and lets every key be a 48dp+
 * target with M3 state layers and haptics. Long-pressing backspace clears.
 */
@Composable
fun NumericKeypad(
    onDigit: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(".", "0", BACKSPACE),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (key in row) {
                    if (key == BACKSPACE) {
                        KeypadKey(
                            modifier = Modifier.weight(1f),
                            tonal = true,
                            onClick = onBackspace,
                            onLongClick = onClear,
                            label = "删除，长按清空",
                        ) {
                            Icon(AppIcons.Backspace, contentDescription = null)
                        }
                    } else {
                        KeypadKey(
                            modifier = Modifier.weight(1f),
                            tonal = false,
                            onClick = { onDigit(key[0]) },
                            label = if (key == ".") "小数点" else key,
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.headlineSmall,
                                color = LocalContentColor.current,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadKey(
    onClick: () -> Unit,
    label: String,
    tonal: Boolean,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = if (tonal) scheme.secondaryContainer else scheme.surfaceContainerHighest,
        contentColor = if (tonal) scheme.onSecondaryContainer else scheme.onSurface,
        modifier = modifier.aspectRatio(1.6f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                    onLongClick = onLongClick?.let { action ->
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            action()
                        }
                    },
                )
                .semantics { contentDescription = label }
                .padding(4.dp),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

private const val BACKSPACE = "\u232B"
