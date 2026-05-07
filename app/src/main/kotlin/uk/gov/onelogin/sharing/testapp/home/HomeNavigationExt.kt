package uk.gov.onelogin.sharing.testapp.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.SelectCredentialAttributesNavigationExt.navigateToVerifierAttributesSelection
import uk.gov.onelogin.sharing.testapp.credential.select.SelectCredentialNavigationExt.navigateToHolderCredentialSelection

object HomeNavigationExt {

    internal fun NavGraphBuilder.configureTestAppHomeScreen(controller: NavController) {
        composable<HomeRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }
            TestAppScreen(
                modifier = Modifier.fillMaxSize(),
                onStartHolderJourney = {
                    scope.launch {
                        controller.navigateToHolderCredentialSelection()
                    }
                },
                onStartVerifierJourney = {
                    scope.launch {
                        controller.navigateToVerifierAttributesSelection()
                    }
                }
            )
        }
    }
}
