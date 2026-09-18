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
import uk.gov.onelogin.sharing.testapp.verifier.auth.issuer.IssuerRootCertificateProvider

object MainActivityRoutes {
    @Suppress("DEPRECATION")
    internal fun NavGraphBuilder.configureTestAppRoutes(
        mockCredentials: List<MockCredentialState>,
        navController: NavController,
        presentCredentialSdk: PresentCredentialSdk,
        verifyCredentialSdk: VerifyCredentialSdk,
        issuerRootCertificateProvider: IssuerRootCertificateProvider
    ) {
        configureTestAppHomeScreen(navController)
        configureSelectMockCredentialDialog(
            controller = navController,
            mockCredentials = mockCredentials
        )
        configureHolderJourneyWrapper { credential ->
            presentCredentialSdk
                .presenter(
                    SampleCredentialProvider(
                        credential
                    )
                )
        }
        configureVerifierAttributesSelection(navController)
        configureVerifierJourneyWrapper { _, verificationRequest ->
            verifyCredentialSdk.verifier(
                VerifierConfig(
                    verificationRequest = verificationRequest,
                    trustedRootCertificate = issuerRootCertificateProvider
                        .trustedRootCertificate()
                )
            )
        }
    }
}
