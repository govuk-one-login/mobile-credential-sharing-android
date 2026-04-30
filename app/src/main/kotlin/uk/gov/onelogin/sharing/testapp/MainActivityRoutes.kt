package uk.gov.onelogin.sharing.testapp

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.credential.SampleCredentialProvider
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.SelectCredentialAttributesNavigationExt.configureVerifierAttributesSelection
import uk.gov.onelogin.sharing.testapp.credential.select.SelectCredentialNavigationExt.configureSelectMockCredentialDialog
import uk.gov.onelogin.sharing.testapp.holder.HolderTestAppJourneyNavigationExt.configureHolderJourneyWrapper
import uk.gov.onelogin.sharing.testapp.home.HomeNavigationExt.configureTestAppHomeScreen
import uk.gov.onelogin.sharing.testapp.verifier.VerifierTestAppJourneyNavigationExt.configureVerifierJourneyWrapper

object MainActivityRoutes {
    internal fun NavGraphBuilder.configureTestAppRoutes(
        mockCredentials: List<MockCredentialState>,
        navController: NavController,
        presentCredentialSdk: PresentCredentialSdk,
        verifyCredentialSdk: VerifyCredentialSdk
    ) {
        configureTestAppHomeScreen(navController)
        configureSelectMockCredentialDialog(
            controller = navController,
            mockCredentials = mockCredentials
        )
        configureHolderJourneyWrapper(navController) { credential ->
            presentCredentialSdk
                .presenter(
                    SampleCredentialProvider(
                        credential
                    )
                )
        }
        configureVerifierAttributesSelection(navController)
        configureVerifierJourneyWrapper(navController) { verificationRequest ->
            verifyCredentialSdk.verifier(
                VerifierConfig(
                    verificationRequest = verificationRequest,
                    trustedCertificates = emptyList()
                )
            )
        }
    }
}
