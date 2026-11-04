package uk.gov.onelogin.sharing.core.presentation.theme

import androidx.compose.ui.graphics.Color
import uk.gov.android.ui.theme.m3.ColorPair

/**
 * These GDS palette values are not provided by the shared UI library (mobile-android-ui),
 */
@Suppress("MagicNumber")
object SharingColors {
    val grey3 = Color(0xFFB1B4B6)
    val grey4 = Color(0xFFF3F2F1)
    val white = Color(0xFFFFFFFF)
    val charcoal1 = Color(0xFF0B0C0C)
    val charcoal2 = Color(0xFF3C3C3C)
}

object Backgrounds {
    val sharingCard = ColorPair(light = SharingColors.grey4, dark = SharingColors.charcoal2)
    val sharingCardRow = ColorPair(light = SharingColors.white, dark = SharingColors.charcoal1)
}

object Dividers {
    val sharingCard = ColorPair(light = SharingColors.grey3, dark = SharingColors.grey3)
}
