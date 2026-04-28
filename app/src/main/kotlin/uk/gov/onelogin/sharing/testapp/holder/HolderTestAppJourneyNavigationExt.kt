package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlin.reflect.typeOf
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.testapp.credential.MockCredential
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState.Companion.MockCredentialStateType

object HolderTestAppJourneyNavigationExt {
    fun NavController.navigateToTestAppHolderJourney(
        state: MockCredentialState,
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        HolderTestAppJourney(state = state),
        options
    )

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

            val presenter = remember {
                component(arguments.state.toCredential(context))
            }

            HolderTestAppJourneyScreen(
                component = presenter,
                modifier = Modifier.fillMaxSize()
            ) {
                presenter.orchestrator.cancel()
                navController.popBackStack()
            }
        }
    }
}
