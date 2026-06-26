package com.ahoura.notekeeper.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.ahoura.notekeeper.domain.model.NoteColor

/**
 * Maps the persisted [NoteColor] palette to actual Compose colors. The light hex values come
 * straight from the domain enum; dark equivalents are hand-tuned so cards stay legible and don't
 * glare in dark mode.
 */
private val darkVariants: Map<NoteColor, Color> = mapOf(
    NoteColor.DEFAULT to SurfaceDark,
    NoteColor.RED to Color(0xFF5C2B29),
    NoteColor.PINK to Color(0xFF5A2D45),
    NoteColor.ORANGE to Color(0xFF5C4326),
    NoteColor.YELLOW to Color(0xFF57532B),
    NoteColor.GREEN to Color(0xFF2E4730),
    NoteColor.TEAL to Color(0xFF28484C),
    NoteColor.BLUE to Color(0xFF28384C),
    NoteColor.PURPLE to Color(0xFF432C4A),
    NoteColor.GRAY to Color(0xFF3C4043),
    NoteColor.CHARCOAL to Color(0xFF263238)
)

private fun NoteColor.lightColor(): Color = Color(android.graphics.Color.parseColor(hexValue))

/** Resolves the card/background color for this note color in the active theme. */
fun NoteColor.toComposeColor(darkTheme: Boolean): Color =
    if (darkTheme) darkVariants.getValue(this) else lightColor()

/** Picks a legible on-color (near-black or near-white) for the given surface color. */
fun Color.contentColorFor(): Color =
    if (luminance() > 0.5f) Color(0xFF202124) else Color(0xFFE8EAED)

/** Convenience: the on-color to use atop this note color in the active theme. */
fun NoteColor.onColor(darkTheme: Boolean): Color = toComposeColor(darkTheme).contentColorFor()
