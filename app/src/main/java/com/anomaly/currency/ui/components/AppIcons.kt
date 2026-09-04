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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Hand-authored Material Symbols paths.
 *
 * These live here instead of pulling in `material-icons-extended`, which would
 * add several MB of unused vectors to the APK for the handful of glyphs this
 * app needs.
 */
object AppIcons {

    val Backspace: ImageVector by lazy {
        icon(
            "backspace",
            "M22 3H7c-.69 0-1.23.35-1.59.88L0 12l5.41 8.11c.36.53.9.89 1.59.89h15c1.1 0 " +
                "2-.9 2-2V5c0-1.1-.9-2-2-2zm-3 12.59L17.59 17 14 13.41 10.41 17 9 15.59 " +
                "12.59 12 9 8.41 10.41 7 14 10.59 17.59 7 19 8.41 15.41 12 19 15.59z",
        )
    }

    val SwapVert: ImageVector by lazy {
        icon(
            "swap_vert",
            "M16 17.01V10h-2v7.01h-3L15 21l4-3.99h-3zM9 3L5 6.99h3V14h2V6.99h3L9 3z",
        )
    }

    val Refresh: ImageVector by lazy {
        icon(
            "refresh",
            "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-8 8s3.57 8 8 8c3.73 0 " +
                "6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 " +
                "6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z",
        )
    }

    val Search: ImageVector by lazy {
        icon(
            "search",
            "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 " +
                "3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 " +
                "4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 " +
                "14 9.5 11.99 14 9.5 14z",
        )
    }

    val Close: ImageVector by lazy {
        icon(
            "close",
            "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 " +
                "17.59 19 19 17.59 13.41 12z",
        )
    }

    val ArrowBack: ImageVector by lazy {
        icon(
            "arrow_back",
            "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z",
        )
    }

    val Star: ImageVector by lazy {
        icon(
            "star",
            "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 " +
                "4.73L5.82 21z",
        )
    }

    val StarOutline: ImageVector by lazy {
        icon(
            "star_outline",
            "M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 " +
                "21l-1.63-7.03L22 9.24zM12 15.4l-3.76 2.271-1.42-4.28 3.32-2.88-4.38-.38L12 " +
                "6.1l1.71 4.04 4.38.38-3.32 2.88 1 4.28L12 15.4z",
        )
    }

    val LightMode: ImageVector by lazy {
        icon(
            "light_mode",
            "M12 7c-2.76 0-5 2.24-5 5s2.24 5 5 5 5-2.24 5-5-2.24-5-5-5zM2 13h2c.55 0 " +
                "1-.45 1-1s-.45-1-1-1H2c-.55 0-1 .45-1 1s.45 1 1 1zm18 0h2c.55 0 1-.45 " +
                "1-1s-.45-1-1-1h-2c-.55 0-1 .45-1 1s.45 1 1 1zM11 2v2c0 .55.45 1 1 1s1-.45 " +
                "1-1V2c0-.55-.45-1-1-1s-1 .45-1 1zm0 18v2c0 .55.45 1 1 1s1-.45 " +
                "1-1v-2c0-.55-.45-1-1-1s-1 .45-1 1zM5.99 4.58a.996.996 0 1 0-1.41 1.41l1.06 " +
                "1.06c.39.39 1.03.39 1.41 0s.39-1.03 0-1.41L5.99 4.58zm12.37 12.37a.996.996 " +
                "0 1 0-1.41 1.41l1.06 1.06c.39.39 1.03.39 1.41 0a.996.996 0 0 0 " +
                "0-1.41l-1.06-1.06zm1.06-10.96a.996.996 0 1 0-1.41-1.41l-1.06 1.06c-.39.39" +
                "-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06zM7.05 18.36a.996.996 0 1 " +
                "0-1.41-1.41l-1.06 1.06c-.39.39-.39 1.03 0 1.41s1.03.39 1.41 0l1.06-1.06z",
        )
    }

    val DarkMode: ImageVector by lazy {
        icon(
            "dark_mode",
            "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9 9-4.03 9-9c0-.46-.04-.92-.1-1.36-.98 " +
                "1.37-2.58 2.26-4.4 2.26-2.98 0-5.4-2.42-5.4-5.4 0-1.81.89-3.42 " +
                "2.26-4.4-.44-.06-.9-.1-1.36-.1z",
        )
    }

    val Contrast: ImageVector by lazy {
        icon(
            "contrast",
            "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 " +
                "18V4c4.41 0 8 3.59 8 8s-3.59 8-8 8z",
        )
    }

    val Palette: ImageVector by lazy {
        icon(
            "palette",
            "M12 3c-4.97 0-9 4.03-9 9s4.03 9 9 9c.83 0 1.5-.67 1.5-1.5 0-.39-.15-.74" +
                "-.39-1.01-.23-.26-.38-.61-.38-.99 0-.83.67-1.5 1.5-1.5H16c2.76 0 5-2.24 " +
                "5-5 0-4.42-4.03-8-9-8zm-5.5 9c-.83 0-1.5-.67-1.5-1.5S5.67 9 6.5 9 8 9.67 " +
                "8 10.5 7.33 12 6.5 12zm3-4C8.67 8 8 7.33 8 6.5S8.67 5 9.5 5s1.5.67 1.5 " +
                "1.5S10.33 8 9.5 8zm5 0c-.83 0-1.5-.67-1.5-1.5S13.67 5 14.5 5s1.5.67 1.5 " +
                "1.5S15.33 8 14.5 8zm3 4c-.83 0-1.5-.67-1.5-1.5S16.67 9 17.5 9s1.5.67 1.5 " +
                "1.5S18.33 12 17.5 12z",
        )
    }

    val CloudOff: ImageVector by lazy {
        icon(
            "cloud_off",
            "M24 15c0-2.64-2.05-4.78-4.65-4.96C18.67 6.59 15.64 4 12 4c-1.33 " +
                "0-2.57.36-3.65.97l1.49 1.49C10.51 6.17 11.23 6 12 6c3.04 0 5.5 2.46 5.5 " +
                "5.5v.5H19c1.66 0 3 1.34 3 3 0 1.13-.64 2.11-1.56 2.62l1.45 1.45C23.16 " +
                "18.16 24 16.68 24 15zM4.41 3.86L3 5.27l2.77 2.77h-.42C2.5 8.24.24 " +
                "10.65.03 13.56-.19 16.69 2.29 19 5.35 19h12.38l2 2 1.41-1.41L4.41 " +
                "3.86zM5.35 17c-1.85 0-3.35-1.5-3.35-3.35 0-1.85 1.5-3.35 3.35-3.35h2.42L14.18 " +
                "17H5.35z",
        )
    }

    private fun icon(name: String, pathData: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            addPath(
                pathData = addPathNodes(pathData),
                fill = SolidColor(Color.Black),
            )
        }.build()
}
