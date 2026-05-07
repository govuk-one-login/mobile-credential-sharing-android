package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.holder.HolderTestAppJourneyNavigationExt.navigateToTestAppHolderJourney
import uk.gov.onelogin.sharing.testapp.home.HomeRoute

object SelectCredentialNavigationExt {
    fun NavController.navigateToHolderCredentialSelection(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        SelectCredentialRoute,
        options
    )

    internal fun NavGraphBuilder.configureSelectMockCredentialDialog(
        controller: NavController,
        mockCredentials: List<MockCredentialState>
    ) {
        dialog<SelectCredentialRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }

            SelectCredentialsScreen(
                credentials = mockCredentials,
                onSelectCredential = { selectedCredential ->
                    scope.launch {
                        controller.navigateToTestAppHolderJourney(selectedCredential) {
                            popUpTo<HomeRoute> {
                                inclusive = false
                            }
                        }
                    }
                }
            )
        }
    }
}
