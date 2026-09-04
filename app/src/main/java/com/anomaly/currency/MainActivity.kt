package com.anomaly.currency

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anomaly.currency.ui.ConverterViewModel
import com.anomaly.currency.ui.CurrencyApp
import com.anomaly.currency.ui.theme.CurrencyTheme
import com.anomaly.currency.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Transparent system bars; icon tint is re-evaluated per theme below.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT),
        )
        setContent {
            val vm: ConverterViewModel = viewModel(factory = ConverterViewModel.Factory)
            val state by vm.state.collectAsStateWithLifecycle()

            val systemDark = isSystemInDarkTheme()
            val dark = when (state.themeMode) {
                ThemeMode.System -> systemDark
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }

            // Keep OS status/nav icon contrast in sync with the in-app theme;
            // otherwise forcing dark mode on a light system leaves black icons
            // sitting on a near-black surface.
            SideEffect {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !dark
                controller.isAppearanceLightNavigationBars = !dark
            }

            CurrencyTheme(
                themeMode = state.themeMode,
                dynamicColor = state.dynamicColor,
            ) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    CurrencyApp(state = state, viewModel = vm)
                }
            }
        }
    }

    private companion object {
        const val TRANSPARENT = 0x00000000
    }
}
