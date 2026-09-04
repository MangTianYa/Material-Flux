package com.anomaly.currency.ui.screens

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.anomaly.currency.ui.components.AppIcons
import com.anomaly.currency.ui.theme.ThemeMode

/**
 * Appearance sheet: dark-mode selection plus dynamic color.
 *
 * Dark mode is a three-way choice rather than a boolean because "follow system"
 * is the Android-idiomatic default and users still need a hard override.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSheet(
    themeMode: ThemeMode,
    dynamicColor: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
        ) {
            Text(
                text = "外观",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "深色模式与取色方案",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                ThemeOption(
                    modifier = Modifier.weight(1f),
                    label = "跟随系统",
                    icon = AppIcons.Contrast,
                    selected = themeMode == ThemeMode.System,
                    onClick = { onThemeModeChange(ThemeMode.System) },
                )
                ThemeOption(
                    modifier = Modifier.weight(1f),
                    label = "浅色",
                    icon = AppIcons.LightMode,
                    selected = themeMode == ThemeMode.Light,
                    onClick = { onThemeModeChange(ThemeMode.Light) },
                )
                ThemeOption(
                    modifier = Modifier.weight(1f),
                    label = "深色",
                    icon = AppIcons.DarkMode,
                    selected = themeMode == ThemeMode.Dark,
                    onClick = { onThemeModeChange(ThemeMode.Dark) },
                )
            }

            Spacer(Modifier.height(20.dp))

            DynamicColorRow(
                enabled = dynamicColor,
                supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                onChange = onDynamicColorChange,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ThemeOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHighest,
        contentColor = if (selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
        border = if (selected) BorderStroke(2.dp, scheme.primary) else null,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun DynamicColorRow(
    enabled: Boolean,
    supported: Boolean,
    onChange: (Boolean) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = scheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                AppIcons.Palette,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "动态取色",
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.onSurface,
                )
                Text(
                    text = if (supported) {
                        "使用壁纸配色（Material You）"
                    } else {
                        "需要 Android 12 及以上"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled && supported,
                onCheckedChange = onChange,
                enabled = supported,
            )
        }
    }
}
