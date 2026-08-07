package uk.gov.onelogin.sharing.ui.impl

import android.R.drawable.ic_menu_close_clear_cancel
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.MonitorHolderSessionState
import uk.gov.onelogin.sharing.holder.cancellation.dialog.HolderCancellationDialogNavigationExt.navigateToHolderUserCancellationDialog
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
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
    val orchestrator = uiGraph.holderOrchestrator()

    ShareCredential(
        orchestrator = orchestrator,
        holderSessionState = orchestrator.holderSessionState,
        modifier = modifier,
        navController = navController,
        viewModelFactory = uiGraph.metroViewModelFactory
    )
}

@Composable
internal fun ShareCredential(
    orchestrator: Orchestrator.Holder,
    holderSessionState: StateFlow<HolderSessionState>,
    viewModelFactory: MetroViewModelFactory,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    val scope = rememberCoroutineScope { defaultDispatcher }
    val state: HolderSessionState by holderSessionState.collectAsStateWithLifecycle()

    val onCancel: () -> Unit = {
        if (state.shouldConfirmCancellation()) {
            navController.navigateToHolderUserCancellationDialog()
        } else {
            orchestrator.cancel()
        }
    }

    BackHandler(state.userCanCancel(), onBack = onCancel)

    MonitorHolderSessionState(
        holderSessionState = holderSessionState,
        navController = navController
    )

    LaunchedEffect(Unit) {
        scope.launch {
            if (holderSessionState.value is HolderSessionState.NotStarted) {
                orchestrator.start()
            }
        }
    }

    CompositionLocalProvider(
        LocalMetroViewModelFactory provides viewModelFactory
    ) {
        Surface(modifier = modifier) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (state.userCanCancel()) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            painter = painterResource(ic_menu_close_clear_cancel),
                            contentDescription = "Close"
                        )
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = HolderRoutes,
                    modifier = Modifier.fillMaxSize()
                ) {
                    configureHolderRoutes()
                }
            }
        }
    }
}
