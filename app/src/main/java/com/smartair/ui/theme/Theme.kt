package com.smartair.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp

// Google Fonts provider for Plus Jakarta Sans
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.smartair.R.array.com_google_android_gms_fonts_certs
)

val PlusJakartaSans = FontFamily(
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.Bold),
)


object SmartAirThemeState {
    var isDark by mutableStateOf(false)
}

// ─── Stitch "Aether Ambient" Color Palette ───
object SmartAirColors {
    // Primary teal tones
    val Primary get() = if (SmartAirThemeState.isDark) Color(0xFF89F5E7) else Color(0xFF00685F)
    val PrimaryContainer get() = if (SmartAirThemeState.isDark) Color(0xFF004F47) else Color(0xFF008378)
    val OnPrimary get() = if (SmartAirThemeState.isDark) Color(0xFF003731) else Color.White
    val OnPrimaryContainer get() = if (SmartAirThemeState.isDark) Color(0xFF89F5E7) else Color(0xFFF4FFFC)

    // Secondary
    val Secondary get() = if (SmartAirThemeState.isDark) Color(0xFF71F8E4) else Color(0xFF006B5F)
    val SecondaryContainer get() = if (SmartAirThemeState.isDark) Color(0xFF005047) else Color(0xFF6DF5E1)
    val OnSecondaryContainer get() = if (SmartAirThemeState.isDark) Color(0xFF71F8E4) else Color(0xFF006F64)

    // Tertiary
    val Tertiary get() = if (SmartAirThemeState.isDark) Color(0xFF9CF2E8) else Color(0xFF006860)
    val TertiaryContainer get() = if (SmartAirThemeState.isDark) Color(0xFF00504B) else Color(0xFF248279)

    // Surface hierarchy
    val Surface get() = if (SmartAirThemeState.isDark) Color(0xFF191C1C) else Color(0xFFFAF8FF)
    val SurfaceContainerLowest get() = if (SmartAirThemeState.isDark) Color(0xFF0F1414) else Color.White
    val SurfaceContainerLow get() = if (SmartAirThemeState.isDark) Color(0xFF171A1A) else Color(0xFFF2F3FF)
    val SurfaceContainer get() = if (SmartAirThemeState.isDark) Color(0xFF1E2121) else Color(0xFFEAEDFF)
    val SurfaceContainerHigh get() = if (SmartAirThemeState.isDark) Color(0xFF282B2B) else Color(0xFFE2E7FF)
    val SurfaceContainerHighest get() = if (SmartAirThemeState.isDark) Color(0xFF333636) else Color(0xFFDAE2FD)
    val SurfaceVariant get() = if (SmartAirThemeState.isDark) Color(0xFF3F4948) else Color(0xFFDAE2FD)

    // On-surface
    val OnSurface get() = if (SmartAirThemeState.isDark) Color(0xFFE0E3E1) else Color(0xFF131B2E)
    val OnSurfaceVariant get() = if (SmartAirThemeState.isDark) Color(0xFFBEC9C7) else Color(0xFF3D4947)

    // Inverse
    val InverseSurface get() = if (SmartAirThemeState.isDark) Color(0xFFE0E3E1) else Color(0xFF283044)
    val InverseOnSurface get() = if (SmartAirThemeState.isDark) Color(0xFF191C1C) else Color(0xFFEEF0FF)
    val InversePrimary get() = if (SmartAirThemeState.isDark) Color(0xFF00685F) else Color(0xFF6BD8CB)

    // Error
    val Error get() = if (SmartAirThemeState.isDark) Color(0xFFFFB4AB) else Color(0xFFBA1A1A)
    val ErrorContainer get() = if (SmartAirThemeState.isDark) Color(0xFF93000A) else Color(0xFFFFDAD6)
    val OnError get() = if (SmartAirThemeState.isDark) Color(0xFF690005) else Color.White
    val OnErrorContainer get() = if (SmartAirThemeState.isDark) Color(0xFFFFDAD6) else Color(0xFF93000A)

    // Outline
    val Outline get() = if (SmartAirThemeState.isDark) Color(0xFF899391) else Color(0xFF6D7A77)
    val OutlineVariant get() = if (SmartAirThemeState.isDark) Color(0xFF3F4948) else Color(0xFFBCC9C6)

    // Status colors (from Stitch design system)
    val StatusOptimal get() = Color(0xFF10B981)   // Emerald
    val StatusWarning get() = Color(0xFFF59E0B)   // Amber
    val StatusCritical get() = Color(0xFFEF4444)  // Red

    // Accent / brand
    val BrandAccent get() = if (SmartAirThemeState.isDark) Color(0xFF2DD4BF) else Color(0xFF0D9488)     // Teal 600
    val BrandHighlight get() = if (SmartAirThemeState.isDark) Color(0xFF5EEAD4) else Color(0xFF14B8A6)  // Teal 500

    // Fixed
    val PrimaryFixed = Color(0xFF89F5E7)
    val PrimaryFixedDim = Color(0xFF6BD8CB)
    val SecondaryFixed = Color(0xFF71F8E4)
    val TertiaryFixed = Color(0xFF9CF2E8)
}

// Material 3 color scheme using Stitch palette (Light)
val SmartAirLightColors = lightColorScheme(
    primary = SmartAirColors.Primary,
    primaryContainer = SmartAirColors.PrimaryContainer,
    onPrimary = SmartAirColors.OnPrimary,
    onPrimaryContainer = SmartAirColors.OnPrimaryContainer,
    secondary = SmartAirColors.Secondary,
    secondaryContainer = SmartAirColors.SecondaryContainer,
    onSecondaryContainer = SmartAirColors.OnSecondaryContainer,
    tertiary = SmartAirColors.Tertiary,
    tertiaryContainer = SmartAirColors.TertiaryContainer,
    surface = SmartAirColors.Surface,
    surfaceVariant = SmartAirColors.SurfaceVariant,
    onSurface = SmartAirColors.OnSurface,
    onSurfaceVariant = SmartAirColors.OnSurfaceVariant,
    inverseSurface = SmartAirColors.InverseSurface,
    inverseOnSurface = SmartAirColors.InverseOnSurface,
    inversePrimary = SmartAirColors.InversePrimary,
    error = SmartAirColors.Error,
    errorContainer = SmartAirColors.ErrorContainer,
    onError = SmartAirColors.OnError,
    onErrorContainer = SmartAirColors.OnErrorContainer,
    outline = SmartAirColors.Outline,
    outlineVariant = SmartAirColors.OutlineVariant,
    background = SmartAirColors.Surface,
    onBackground = SmartAirColors.OnSurface,
)

// Material 3 color scheme using Stitch palette (Dark)
val SmartAirDarkColors = darkColorScheme(
    primary = SmartAirColors.PrimaryFixed,
    primaryContainer = Color(0xFF004F47),
    onPrimary = Color(0xFF003731),
    onPrimaryContainer = SmartAirColors.PrimaryFixed,
    secondary = SmartAirColors.SecondaryFixed,
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = SmartAirColors.SecondaryFixed,
    tertiary = SmartAirColors.TertiaryFixed,
    tertiaryContainer = Color(0xFF00504B),
    surface = Color(0xFF191C1C),
    surfaceVariant = Color(0xFF3F4948),
    onSurface = Color(0xFFE0E3E1),
    onSurfaceVariant = Color(0xFFBEC9C7),
    inverseSurface = Color(0xFFE0E3E1),
    inverseOnSurface = Color(0xFF191C1C),
    inversePrimary = SmartAirColors.Primary,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF899391),
    outlineVariant = Color(0xFF3F4948),
    background = Color(0xFF191C1C),
    onBackground = Color(0xFFE0E3E1),
)

// Typography matching Stitch's Plus Jakarta Sans system
val SmartAirTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        lineHeight = 52.sp,
        letterSpacing = (-0.03).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.01).sp
    ),
    titleLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.02).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.01).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.08.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.02.sp
    ),
)

@Composable
fun SmartAirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        SmartAirDarkColors
    } else {
        SmartAirLightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SmartAirTypography,
        content = content
    )
}
