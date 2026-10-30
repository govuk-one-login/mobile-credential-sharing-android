package uk.gov.onelogin.sharing.ui.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.android.ui.theme.m3.GdsLocalColorScheme
import uk.gov.logging.api.analytics.logging.v3.AnalyticsLogger
import uk.gov.onelogin.sharing.holder.HolderRoutes
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.MonitorHolderSessionState
import uk.gov.onelogin.sharing.holder.cancellation.dialog.HolderCancellationDialogNavigationExt.navigateToHolderUserCancellationDialog
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.ui.impl.ShareCredentialAnalytics.closeButton
import uk.gov.onelogin.sharing.ui.impl.di.HolderUiGraph
import uk.gov.onelogin.sharing.uiimpl.R

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
        analyticsLogger = uiGraph.appGraph().analyticsLogger(),
        orchestrator = orchestrator,
        holderSessionState = orchestrator.holderSessionState,
        modifier = modifier,
        navController = navController,
        viewModelFactory = uiGraph.metroViewModelFactory
    )
}

private fun logCloseButtonEvent(analyticsLogger: AnalyticsLogger) {
    analyticsLogger.closeButton()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongMethod")
internal fun ShareCredential(
    analyticsLogger: AnalyticsLogger,
    orchestrator: Orchestrator.Holder,
    holderSessionState: StateFlow<HolderSessionState>,
    viewModelFactory: MetroViewModelFactory,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    uiDispatcher: CoroutineContext = Dispatchers.Main
) {
    val scope = rememberCoroutineScope { defaultDispatcher }
    val state: HolderSessionState by holderSessionState.collectAsStateWithLifecycle()

    val onCancel: () -> Unit = {
        scope.launch {
            if (state.shouldConfirmCancellation()) {
                withContext(uiDispatcher) {
                    navController.navigateToHolderUserCancellationDialog()
                }
            } else {
                orchestrator.cancel()
            }
        }
    }

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                if (state.userCanCancel()) {
                    CloseButtonAppBar(
                        onCancel = {
                            scope.launch { logCloseButtonEvent(analyticsLogger) }
                            onCancel()
                        }
                    )
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

    BackHandler(state.userCanCancel(), onBack = onCancel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CloseButtonAppBar(
    modifier: Modifier = Modifier,
    scrollBehaviour: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    onCancel: () -> Unit = {}
) {
    TopAppBar(
        title = {},
        modifier = modifier,
        navigationIcon = {
            IconButton(onCancel) {
                Icon(
                    painter = painterResource(R.drawable.filled_close),
                    contentDescription = "Close",
                    tint = GdsLocalColorScheme.current.topBarIcon
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = GdsLocalColorScheme.current.topBarScrolledBackground
        ),
        scrollBehavior = scrollBehaviour
    )
}
