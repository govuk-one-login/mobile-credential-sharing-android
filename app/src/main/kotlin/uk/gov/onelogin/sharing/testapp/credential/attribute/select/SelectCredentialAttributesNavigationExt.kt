package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.testapp.home.HomeRoute
import uk.gov.onelogin.sharing.testapp.verifier.VerifierTestAppJourneyNavigationExt.navigateToTestAppVerifierJourney

object SelectCredentialAttributesNavigationExt {

    fun NavController.navigateToVerifierAttributesSelection(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        SelectCredentialAttributesRoute,
        options
    )

    internal fun NavGraphBuilder.configureVerifierAttributesSelection(controller: NavController) {
        dialog<SelectCredentialAttributesRoute> {
            val scope = rememberCoroutineScope { Dispatchers.Main }
            SelectCredentialAttributesScreen(
                onSelectAttributeGroup = { attributeGroup ->
                    scope.launch {
                        controller.navigateToTestAppVerifierJourney(
                            VerificationRequest.typed(
                                DocumentType.Mdl,
                                attributeGroup = attributeGroup
                            )
                        ) {
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
