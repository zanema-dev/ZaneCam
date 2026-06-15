package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = CineRed,
    secondary = GraphiteGray,
    tertiary = GoldAccent,
    background = PureBlack,
    surface = DeepGray,
    onPrimary = LightGray,
    onSecondary = LightGray,
    onTertiary = PureBlack,
    onBackground = LightGray,
    onSurface = LightGray
  )

private val LightColorScheme = DarkColorScheme // Standard cinema UI is always dark for optimal focus

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Always dark for cinematic camera context
  dynamicColor: Boolean = false, // Force custom branding
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
