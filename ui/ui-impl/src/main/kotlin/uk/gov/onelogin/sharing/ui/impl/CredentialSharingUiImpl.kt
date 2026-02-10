package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.material3.Text
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

class CredentialSharingUiImpl : CredentialSharingUi {
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
            Text("Hello World!")
        }
    }
}
