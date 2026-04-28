package uk.gov.onelogin.sharing.testapp

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.credential.SampleCredentialProvider
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.SelectCredentialAttributesNavigationExt.configureVerifierAttributesSelection
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.SelectCredentialAttributesNavigationExt.navigateToVerifierAttributesSelection
import uk.gov.onelogin.sharing.testapp.credential.select.SelectCredentialNavigationExt.configureSelectMockCredentialDialog
import uk.gov.onelogin.sharing.testapp.credential.select.SelectCredentialNavigationExt.navigateToHolderCredentialSelection
import uk.gov.onelogin.sharing.testapp.holder.HolderTestAppJourneyNavigationExt.configureHolderJourneyWrapper
import uk.gov.onelogin.sharing.testapp.holder.HolderTestAppJourneyNavigationExt.navigateToTestAppHolderJourney
import uk.gov.onelogin.sharing.testapp.home.HomeNavigationExt.configureTestAppHomeScreen
import uk.gov.onelogin.sharing.testapp.home.HomeRoute
import uk.gov.onelogin.sharing.testapp.verifier.VerifierTestAppJourneyNavigationExt.configureVerifierJourneyWrapper
import uk.gov.onelogin.sharing.testapp.verifier.VerifierTestAppJourneyNavigationExt.navigateToTestAppVerifierJourney

object MainActivityRoutes {
    internal fun NavGraphBuilder.configureTestAppRoutes(
        mockCredentials: List<MockCredentialState>,
        navController: NavController,
        presentCredentialSdk: PresentCredentialSdk,
        verifyCredentialSdk: VerifyCredentialSdk
    ) {
        configureTestAppHomeScreen(
            onStartHolderJourney = {
                navController.navigateToHolderCredentialSelection()
            },
            onStartVerifierJourney = {
                navController.navigateToVerifierAttributesSelection()
            }
        )
        configureSelectMockCredentialDialog(
            mockCredentials = mockCredentials
        ) { selectedCredential ->
            navController.navigateToTestAppHolderJourney(selectedCredential) {
                popUpTo<HomeRoute> {
                    inclusive = false
                }
            }
        }
        configureHolderJourneyWrapper(navController) { credential ->
            presentCredentialSdk
                .presenter(
                    SampleCredentialProvider(
                        credential
                    )
                )
        }
        configureVerifierAttributesSelection { attributeGroup ->
            navController.navigateToTestAppVerifierJourney(
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
