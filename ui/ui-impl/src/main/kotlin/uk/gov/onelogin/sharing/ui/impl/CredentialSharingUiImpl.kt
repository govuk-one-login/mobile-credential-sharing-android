package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi
import uk.gov.onelogin.sharing.ui.impl.di.CredentialSharingUiGraph

/**
 * Implementation of [CredentialSharingUi] that provides the entry point for the
 * Credential Sharing UI.
 *
 * This class handles the creation of the UI dependency graph and sets up
 * the [LocalMetroViewModelFactory] for the UI components.
 */
class CredentialSharingUiImpl : CredentialSharingUi {
    /**
     * Renders the Credential Sharing UI.
     *
     * @param sdk The [CredentialSharingSdk] instance to use for core functionality.
     * @param startDestination The initial screen to display in the Credential Sharing flow.
     * @param modifier The [Modifier] to apply to the UI.
     */
    @Composable
    override fun Render(
        sdk: CredentialSharingSdk,
        startDestination: CredentialSharingDestination,
        modifier: Modifier
    ) {
        val uiGraph = remember(sdk.appGraph) {
            createGraphFactory<CredentialSharingUiGraph.Factory>()
                .create(sdk.appGraph)
        }

        CompositionLocalProvider(
            LocalMetroViewModelFactory provides uiGraph.metroViewModelFactory
        ) {
            CredentialSharingUiNavHost(
                startDestination = startDestination
            )
        }
    }
}
