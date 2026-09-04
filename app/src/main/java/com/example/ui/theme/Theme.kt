package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = BoldDarkPrimary,
    onPrimary = Color.White,
    primaryContainer = BoldDarkSurfaceVariant,
    onPrimaryContainer = Color.White,
    secondary = AmberDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = EmeraldDark,
    background = BoldDarkBackground,
    onBackground = BoldDarkOnBackground,
    surface = BoldDarkSurface,
    onSurface = BoldDarkOnSurface,
    surfaceVariant = BoldDarkSurfaceVariant,
    onSurfaceVariant = BoldDarkOnSurfaceVariant,
    outline = BoldDarkOutline
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BoldPrimary,
    onPrimary = BoldOnPrimary,
    primaryContainer = BoldPrimaryContainer,
    onPrimaryContainer = BoldOnPrimaryContainer,
    secondary = BoldSecondary,
    onSecondary = BoldOnSecondary,
    secondaryContainer = BoldSecondaryContainer,
    onSecondaryContainer = BoldOnSecondaryContainer,
    tertiary = BoldTertiary,
    onTertiary = BoldOnTertiary,
    tertiaryContainer = BoldTertiaryContainer,
    onTertiaryContainer = BoldOnTertiaryContainer,
    background = BoldBackground,
    onBackground = BoldOnBackground,
    surface = BoldSurface,
    onSurface = BoldOnSurface,
    surfaceVariant = BoldSurfaceVariant,
    onSurfaceVariant = BoldOnSurfaceVariant,
    outline = BoldOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
