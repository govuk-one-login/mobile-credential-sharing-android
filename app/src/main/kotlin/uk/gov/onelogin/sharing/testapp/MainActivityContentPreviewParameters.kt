package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination

/**
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] implementation for the
 * [MainActivityContentPreview] composable UI.
 */
data class MainActivityContentPreviewParameters(
    override val values: Sequence<PrimaryTabDestination> =
        PrimaryTabDestination.Companion.entries().asSequence()
) : PreviewParameterProvider<PrimaryTabDestination>
