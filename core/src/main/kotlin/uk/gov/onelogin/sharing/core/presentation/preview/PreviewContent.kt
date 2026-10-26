package uk.gov.onelogin.sharing.core.presentation.preview

import androidx.compose.ui.tooling.preview.Devices.FOLDABLE
import androidx.compose.ui.tooling.preview.Devices.PHONE
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark

/**
 * Shared multi preview annotation that has all the relevant variants
 */
@PreviewLightDark
@PreviewFontScale
@Preview(name = "Phone", device = PHONE, showSystemUi = true)
@Preview(
    name = "Phone - Landscape",
    device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420",
    showSystemUi = true
)
@Preview(name = "Unfolded Foldable", device = FOLDABLE, showSystemUi = true)
@Preview(name = "Welsh", locale = "cy", showBackground = true)
annotation class PreviewContent
