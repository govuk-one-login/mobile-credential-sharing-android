package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.m3.customDynamicColor

/**
 * Wraps content in [GdsTheme] and overrides only the sharing card's colour.
 * These values are not defined by the shared UI library (mobile-android-ui), so
 * they live here and are exposed to composables via MaterialTheme / GdsLocalColorScheme.
 */
@Composable
fun AgreeToShareCardTheme(content: @Composable () -> Unit) {
    GdsTheme {
        CompositionLocalProvider(
            GdsLocalColorScheme provides GdsLocalColorScheme.current.copy(
                cardBackground = customDynamicColor(
                    light = Color(GREY_4),
                    dark = Color(CHARCOAL_2)
                ),
                rowBackground = customDynamicColor(light = Color(WHITE), dark = Color(CHARCOAL_1)),
                dividerCard = customDynamicColor(light = Color(GREY_3), dark = Color(GREY_3))
            ),
            content = content
        )
    }
}

private const val GREY_3 = 0xFFB1B4B6
private const val GREY_4 = 0xFFF3F2F1
private const val WHITE = 0xFFFFFFFF
private const val CHARCOAL_1 = 0xFF0B0C0C
private const val CHARCOAL_2 = 0xFF3C3C3C
