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
    primary = AfriRed, 
    secondary = AfriGold, 
    tertiary = AfriGoldDark,
    background = AfriBlack,
    surface = AfriDarkGray,
    onPrimary = AfriWhite,
    onSecondary = AfriBlack,
    onBackground = AfriWhite,
    onSurface = AfriWhite
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AfriRed,
    secondary = AfriGold,
    tertiary = AfriGoldDark,
    background = AfriLightGray,
    surface = AfriWhite,
    onPrimary = AfriWhite,
    onSecondary = AfriBlack,
    onBackground = AfriBlack,
    onSurface = AfriBlack
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
