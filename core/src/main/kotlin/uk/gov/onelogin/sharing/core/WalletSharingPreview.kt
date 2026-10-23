package uk.gov.onelogin.sharing.core

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes

/**
 * Convenience annotation that applies multiple [Preview] configurations to the attached composable.
 *
 * This includes:
 * - Various screen sizes
 *   - Phone (portrait and landscape)
 *   - Tablet (portrait and landscape)
 *   - Unfolded device
 * - Various font scaling from 85% to 200%
 * - Light / Dark mode
 */
@PreviewScreenSizes
@PreviewFontScale
@PreviewLightDark
annotation class WalletSharingPreview
