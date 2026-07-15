package uk.gov.onelogin.sharing.testapp.holder

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.SuccessReason
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.testapp.credential.MockCredential
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState.Companion.MockCredentialStateType

private const val HOLDER_TEST_APP_JOURNEY_NAV_EXT = "HolderTestAppJourneyNavExt"

object HolderTestAppJourneyNavigationExt {
    fun NavController.navigateToTestAppHolderJourney(
        state: MockCredentialState,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        HolderTestAppJourney(state = state),
        options
    )

    @Suppress("LongMethod")
    internal fun NavGraphBuilder.configureHolderJourneyWrapper(
        navController: NavController,
        component: (MockCredential) -> CredentialPresenter
    ) {
        composable<HolderTestAppJourney>(
            typeMap = mapOf(
                typeOf<MockCredentialState>() to MockCredentialStateType
            )
        ) { navBackStackEntry ->
            val arguments: HolderTestAppJourney = navBackStackEntry.toRoute()
            val context = LocalContext.current
            val scope = rememberCoroutineScope { Dispatchers.Main }

            val credential = remember(arguments.state) {
                try {
                    arguments.state.toCredential(context)
                } catch (e: IllegalArgumentException) {
                    Log.e(
                        HOLDER_TEST_APP_JOURNEY_NAV_EXT,
                        "Invalid credential configuration",
                        e
                    )
                    null
                }
            }

            if (credential == null) {
                Scaffold { contentPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = "Invalid credential"
                        )
                    }
                }
                return@composable
            }

            val presenter by produceCredentialPresenter(
                credential,
                component
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                presenter?.let { presenter ->
                    LaunchedEffect(presenter.orchestrator) {
                        presenter.orchestrator.holderSessionState.collect { state ->
                            if (state is HolderSessionState.Complete.Success &&
                                state.successReason == SuccessReason.Denied
                            ) {
                                presenter.orchestrator.reset()
                                navController.popBackStack()
                            }
                        }
                    }

                    HolderTestAppJourneyScreen(
                        component = presenter,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        scope.launch {
                            presenter.orchestrator.cancel()
                            navController.popBackStack()
                        }
                    }
                } ?: CircularProgressIndicator()
            }
        }
    }

    @Composable
    fun produceCredentialPresenter(
        credential: MockCredential,
        credentialToPresenter: (MockCredential) -> CredentialPresenter,
        dispatcher: CoroutineDispatcher = Dispatchers.Default
    ) = produceState<CredentialPresenter?>(null, credential, credentialToPresenter) {
        value = withContext(dispatcher) { credentialToPresenter(credential) }
    }
}
