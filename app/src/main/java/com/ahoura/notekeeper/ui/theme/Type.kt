package com.ahoura.notekeeper.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ahoura.notekeeper.R

/**
 * English / Latin UI font: Inter — a neutral, highly legible interface typeface. Shipped as a
 * single variable font; each weight pins the `wght` axis via [FontVariation] (variable fonts need
 * minSdk 26, which the app already targets).
 */
@OptIn(ExperimentalTextApi::class)
val InterFontFamily = FontFamily(
    Font(R.font.inter_variable, FontWeight.Normal, variationSettings = wght(400)),
    Font(R.font.inter_variable, FontWeight.Medium, variationSettings = wght(500)),
    Font(R.font.inter_variable, FontWeight.SemiBold, variationSettings = wght(600)),
    Font(R.font.inter_variable, FontWeight.Bold, variationSettings = wght(700))
)

/** Persian font: Vazirmatn — bundled as static Regular / Medium / Bold weights. */
val VazirmatnFontFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

@OptIn(ExperimentalTextApi::class)
private fun wght(weight: Int) = FontVariation.Settings(FontVariation.weight(weight))

/**
 * Builds the app's type scale on top of [fontFamily] so the family can swap per locale.
 *
 * Starts from the Material 3 default [Typography] and stamps [fontFamily] onto **every** style,
 * then overrides the handful we customise. Without the blanket stamp, styles we don't override
 * (e.g. `labelLarge`, which Material buttons/FAB labels use) would keep the platform default font
 * instead of Vazirmatn/Inter — that's why some buttons and inputs looked off.
 */
fun appTypography(fontFamily: FontFamily): Typography {
    val base = Typography()
    fun TextStyle.withFamily() = copy(fontFamily = fontFamily)
    return base.copy(
        displayLarge = base.displayLarge.withFamily().copy(
            fontSize = 57.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 64.sp
        ),
        displayMedium = base.displayMedium.withFamily(),
        displaySmall = base.displaySmall.withFamily(),
        headlineLarge = base.headlineLarge.withFamily(),
        headlineMedium = base.headlineMedium.withFamily().copy(
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 36.sp
        ),
        headlineSmall = base.headlineSmall.withFamily(),
        titleLarge = base.titleLarge.withFamily().copy(
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 28.sp
        ),
        titleMedium = base.titleMedium.withFamily().copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            lineHeight = 24.sp
        ),
        titleSmall = base.titleSmall.withFamily(),
        bodyLarge = base.bodyLarge.withFamily().copy(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 24.sp
        ),
        bodyMedium = base.bodyMedium.withFamily().copy(
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 20.sp
        ),
        bodySmall = base.bodySmall.withFamily(),
        labelLarge = base.labelLarge.withFamily(),
        labelMedium = base.labelMedium.withFamily(),
        labelSmall = base.labelSmall.withFamily().copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    )
}

/** Inter-based type scale for Latin (English) UI. */
val InterTypography: Typography = appTypography(InterFontFamily)

/** Vazirmatn-based type scale for Persian UI. */
val VazirTypography: Typography = appTypography(VazirmatnFontFamily)
