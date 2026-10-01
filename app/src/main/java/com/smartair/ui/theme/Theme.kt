package com.smartair.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

// ─── Stitch "Aether Ambient" Color Palette ───
object SmartAirColors {
    // Primary teal tones
    val Primary = Color(0xFF00685F)
    val PrimaryContainer = Color(0xFF008378)
    val OnPrimary = Color.White
    val OnPrimaryContainer = Color(0xFFF4FFFC)

    // Secondary
    val Secondary = Color(0xFF006B5F)
    val SecondaryContainer = Color(0xFF6DF5E1)
    val OnSecondaryContainer = Color(0xFF006F64)

    // Tertiary
    val Tertiary = Color(0xFF006860)
    val TertiaryContainer = Color(0xFF248279)

    // Surface hierarchy
    val Surface = Color(0xFFFAF8FF)
    val SurfaceContainerLowest = Color.White
    val SurfaceContainerLow = Color(0xFFF2F3FF)
    val SurfaceContainer = Color(0xFFEAEDFF)
    val SurfaceContainerHigh = Color(0xFFE2E7FF)
    val SurfaceContainerHighest = Color(0xFFDAE2FD)
    val SurfaceVariant = Color(0xFFDAE2FD)

    // On-surface
    val OnSurface = Color(0xFF131B2E)
    val OnSurfaceVariant = Color(0xFF3D4947)

    // Inverse
    val InverseSurface = Color(0xFF283044)
    val InverseOnSurface = Color(0xFFEEF0FF)
    val InversePrimary = Color(0xFF6BD8CB)

    // Error
    val Error = Color(0xFFBA1A1A)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnError = Color.White
    val OnErrorContainer = Color(0xFF93000A)

    // Outline
    val Outline = Color(0xFF6D7A77)
    val OutlineVariant = Color(0xFFBCC9C6)

    // Status colors (from Stitch design system)
    val StatusOptimal = Color(0xFF10B981)   // Emerald
    val StatusWarning = Color(0xFFF59E0B)   // Amber
    val StatusCritical = Color(0xFFEF4444)  // Red

    // Accent / brand
    val BrandAccent = Color(0xFF0D9488)     // Teal 600
    val BrandHighlight = Color(0xFF14B8A6)  // Teal 500

    // Fixed
    val PrimaryFixed = Color(0xFF89F5E7)
    val PrimaryFixedDim = Color(0xFF6BD8CB)
    val SecondaryFixed = Color(0xFF71F8E4)
    val TertiaryFixed = Color(0xFF9CF2E8)
}

// Material 3 color scheme using Stitch palette
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
fun SmartAirTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SmartAirLightColors,
        typography = SmartAirTypography,
        content = content
    )
}
