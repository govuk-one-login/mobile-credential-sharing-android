package uk.gov.onelogin.sharing.core.presentation.theme

import androidx.compose.runtime.Composable
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.m3.GdsThemeDefaults
import uk.gov.android.ui.theme.m3.toMappedColors

/**
 * [GdsTheme] with the sharing-specific card colours applied. These deviate from the GDS ui library
 */
@Composable
fun SharingTheme(content: @Composable () -> Unit) {
    GdsTheme(
        extendedColorScheme = GdsThemeDefaults.extendedColorScheme(
            cardBackground = Backgrounds.sharingCard.toMappedColors(),
            rowBackground = Backgrounds.sharingCardRow.toMappedColors(),
            dividerCard = Dividers.sharingCard.toMappedColors()
        ),
        content = content
    )
}
