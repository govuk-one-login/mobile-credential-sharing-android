package uk.gov.onelogin.sharing.core.presentation.preview

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

/**
 * Shared multi preview annotation that has all the relevant variants
 */
@PreviewLightDark
@PreviewFontScale
@PreviewScreenSizes
@Preview(name = "Welsh", locale = "cy", showBackground = true)
annotation class PreviewContent
