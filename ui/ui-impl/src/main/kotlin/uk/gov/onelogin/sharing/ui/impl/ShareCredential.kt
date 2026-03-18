package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.ui.impl.di.HolderUiGraph

/**
 * Composable entry point for the Holder role (credential sharing).
 *
 * Renders the complete Holder UI flow including navigation, allowing users to share
 * their credentials with verifiers via QR code and Bluetooth.
 *
 * @param component The [CredentialPresenter] containing the app graph and configuration.
 * @param modifier Optional [Modifier] to apply to the root composable.
 */
@Composable
fun ShareCredential(component: CredentialPresenter, modifier: Modifier = Modifier) {
    val uiGraph = remember(component.appGraph, component.orchestrator) {
        createGraphFactory<HolderUiGraph.Factory>()
            .create(component.appGraph, component.orchestrator)
    }

    val navController = rememberNavController()

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides uiGraph.metroViewModelFactory
    ) {
        NavHost(
            navController = navController,
            startDestination = HolderRoutes,
            modifier = modifier
        ) {
            configureHolderRoutes()
        }
    }
}
