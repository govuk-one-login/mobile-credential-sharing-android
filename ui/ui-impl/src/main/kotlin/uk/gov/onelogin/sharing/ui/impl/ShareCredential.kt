package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.HolderRoutes.convertSessionStateToNavigation
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
fun ShareCredential(
    component: CredentialPresenter,
    modifier: Modifier = Modifier,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    val uiGraph = remember(component.appGraph, component.orchestrator) {
        createGraphFactory<HolderUiGraph.Factory>()
            .create(component.appGraph, component.orchestrator)
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            component.orchestrator.holderSessionState
                .map { state ->
                    convertSessionStateToNavigation(
                        context,
                        navController,
                        state
                    )
                }.collect { navigationFunction ->
                    withContext(mainDispatcher) {
                        navigationFunction()
                    }
                }
        }
    }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides uiGraph.metroViewModelFactory
    ) {
        NavHost(
            navController = navController,
            startDestination = HolderRoutes,
            modifier = modifier
        ) {
            configureHolderRoutes(navController)
        }
    }
}
