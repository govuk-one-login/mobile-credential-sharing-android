package uk.gov.onelogin.sharing.holder.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.m3.customDynamicColor
import uk.gov.onelogin.sharing.core.presentation.theme.SharingColors

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
                    light = SharingColors.grey4,
                    dark = SharingColors.charcoal2
                ),
                rowBackground = customDynamicColor(
                    light = SharingColors.white,
                    dark = SharingColors.charcoal1
                ),
                dividerCard = customDynamicColor(
                    light = SharingColors.grey3,
                    dark = SharingColors.grey3
                )
            ),
            content = content
        )
    }
}
