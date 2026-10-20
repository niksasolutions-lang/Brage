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

private val DarkColorScheme = darkColorScheme(
    primary = BragePrimaryDark,
    onPrimary = Color(0xFF0F172A),
    primaryContainer = BragePrimaryContainerDark,
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFFFBBF24),
    onSecondary = Color(0xFF1E293B),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF022C22),
    background = BrageBackgroundDark,
    onBackground = BrageOnBackgroundDark,
    surface = BrageSurfaceDark,
    onSurface = BrageOnSurfaceDark,
    surfaceVariant = BrageSurfaceVariantDark,
    onSurfaceVariant = BrageOnSurfaceVariantDark,
    outline = BrageOutlineDark,
    outlineVariant = Color(0xFF1E293B)
)

private val LightColorScheme = lightColorScheme(
    primary = BragePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = BragePrimaryContainer,
    onPrimaryContainer = BrageOnPrimaryContainer,
    secondary = BrageSecondary,
    onSecondary = Color.White,
    secondaryContainer = BrageSecondaryContainer,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = BrageTertiary,
    onTertiary = Color.White,
    tertiaryContainer = BrageTertiaryContainer,
    onTertiaryContainer = Color(0xFF064E3B),
    background = BrageBackground,
    onBackground = BrageOnBackground,
    surface = BrageSurface,
    onSurface = BrageOnSurface,
    surfaceVariant = BrageSurfaceVariant,
    onSurfaceVariant = BrageOnSurfaceVariant,
    outline = BrageOutline,
    outlineVariant = BrageOutlineVariant
)

@Composable
fun BrageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep branded consistent colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
