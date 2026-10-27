package uk.gov.onelogin.sharing.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import uk.gov.android.ui.theme.m3.customDynamicColor

object SharingColors {
    // GDS app palette (ARGB), named to match the design variables.
    private const val GREY_3 = 0xFFB1B4B6
    private const val GREY_4 = 0xFFF3F2F1
    private const val WHITE = 0xFFFFFFFF
    private const val CHARCOAL_1 = 0xFF0B0C0C
    private const val CHARCOAL_2 = 0xFF262626

    val cardBackground: Color
        @Composable get() = customDynamicColor(light = Color(GREY_4), dark = Color(CHARCOAL_2))

    val attributeListBackground: Color
        @Composable get() = customDynamicColor(light = Color(WHITE), dark = Color(CHARCOAL_1))

    val border: Color
        @Composable get() = customDynamicColor(light = Color(GREY_3), dark = Color(GREY_3))

    val divider: Color
        @Composable get() = customDynamicColor(light = Color(GREY_3), dark = Color(GREY_3))
}
