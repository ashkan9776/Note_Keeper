package com.ahoura.notekeeper.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

/**
 * App theme. Defaults to the custom NoteKeep brand palette so the carefully chosen colors show
 * through; dynamic color can be opted into on Android 12+ via [dynamicColor].
 */
@Composable
fun NoteKeeperTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    /** Smoothly cross-fades the palette when the user flips between light and dark. */
    animated: Boolean = true,
    content: @Composable () -> Unit
) {
    val target = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    val colorScheme = if (animated) target.animated() else target

    // Vazirmatn is the single UI typeface for every locale — it carries both Persian and Latin
    // glyphs, so English and Persian share one consistent type scale.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = VazirTypography,
        shapes = AppShapes,
        content = content
    )
}

private const val THEME_ANIM_MS = 450

/**
 * Returns a [ColorScheme] whose most visible slots animate toward [this]. Animating every slot
 * would be wasteful, so only the colors that paint large surfaces or text are tweened; the rest
 * snap instantly, which is imperceptible behind the cross-fading backgrounds.
 */
@Composable
private fun ColorScheme.animated(): ColorScheme {
    val spec = tween<Color>(THEME_ANIM_MS)
    val label = "themeColor"
    val background by animateColorAsState(background, spec, label = label)
    val onBackground by animateColorAsState(onBackground, spec, label = label)
    val surface by animateColorAsState(surface, spec, label = label)
    val onSurface by animateColorAsState(onSurface, spec, label = label)
    val surfaceVariant by animateColorAsState(surfaceVariant, spec, label = label)
    val onSurfaceVariant by animateColorAsState(onSurfaceVariant, spec, label = label)
    val primary by animateColorAsState(primary, spec, label = label)
    val primaryContainer by animateColorAsState(primaryContainer, spec, label = label)
    val outline by animateColorAsState(outline, spec, label = label)
    return copy(
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        primary = primary,
        primaryContainer = primaryContainer,
        outline = outline
    )
}
