package app.retube.core.designsystem

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.retube.core.preferences.ThemeMode

val NovaCrimson = Color(0xFFFF2E54)
val NovaCrimsonDark = Color(0xFFD91B40)
val NovaViolet = Color(0xFF7C4DFF)
val NovaTeal = Color(0xFF00E5FF)

private val StandardDarkColors = darkColorScheme(
    primary = NovaCrimson,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4A0E1B),
    onPrimaryContainer = Color(0xFFFFD9DE),
    secondary = NovaViolet,
    onSecondary = Color.White,
    tertiary = NovaTeal,
    background = Color(0xFF0F0F13),
    onBackground = Color(0xFFF3F3F7),
    surface = Color(0xFF18181F),
    onSurface = Color(0xFFF3F3F7),
    surfaceVariant = Color(0xFF252530),
    onSurfaceVariant = Color(0xFFC5C5D2)
)

private val AmoledDarkColors = StandardDarkColors.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF08080B),
    surfaceVariant = Color(0xFF16161D)
)

private val LightColors = lightColorScheme(
    primary = NovaCrimsonDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9DE),
    onPrimaryContainer = Color(0xFF3F0010),
    secondary = NovaViolet,
    onSecondary = Color.White,
    background = Color(0xFFFAFAFC),
    onBackground = Color(0xFF14141A),
    surface = Color.White,
    onSurface = Color(0xFF14141A),
    surfaceVariant = Color(0xFFEEEEF4),
    onSurfaceVariant = Color(0xFF4A4A58)
)

val ReTubeTypography = Typography(
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun ReTubeTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    dynamicColor: Boolean = false,
    pureBlackAmoled: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StandardDarkColors
        else -> LightColors
    }

    val finalScheme = if (darkTheme && pureBlackAmoled) {
        baseScheme.copy(
            background = Color.Black,
            surface = Color(0xFF08080B),
            surfaceVariant = AmoledDarkColors.surfaceVariant
        )
    } else {
        baseScheme
    }

    MaterialTheme(
        colorScheme = finalScheme,
        typography = ReTubeTypography,
        content = content
    )
}