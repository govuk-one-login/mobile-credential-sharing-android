package uk.gov.onelogin.sharing.testapp

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk
import uk.gov.onelogin.sharing.testapp.credential.AuthCancelledOnceCredentialProvider
import uk.gov.onelogin.sharing.testapp.credential.FailingSignCredentialProvider
import uk.gov.onelogin.sharing.testapp.credential.MockCredential
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialProviderType
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState
import uk.gov.onelogin.sharing.testapp.credential.SampleCredentialProvider
import uk.gov.onelogin.sharing.testapp.credential.attribute.select.SelectCredentialAttributesNavigationExt.configureVerifierAttributesSelection
import uk.gov.onelogin.sharing.testapp.credential.select.SelectCredentialNavigationExt.configureSelectMockCredentialDialog
import uk.gov.onelogin.sharing.testapp.holder.HolderTestAppJourneyNavigationExt.configureHolderJourneyWrapper
import uk.gov.onelogin.sharing.testapp.home.HomeNavigationExt.configureTestAppHomeScreen
import uk.gov.onelogin.sharing.testapp.verifier.VerifierTestAppJourneyNavigationExt.configureVerifierJourneyWrapper
import uk.gov.onelogin.sharing.testapp.verifier.auth.issuer.IssuerRootCertificateProvider

object MainActivityRoutes {
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
                    credentialProviderFor(credential)
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

    /**
     * Selects the Test App [CredentialProvider] implementation for the given [credential] based on
     * its [MockCredential.providerType]. Used to reproduce `sign()` failures without a real
     * local-authentication prompt
     */
    private fun credentialProviderFor(credential: MockCredential): CredentialProvider =
        when (credential.providerType) {
            MockCredentialProviderType.NORMAL -> SampleCredentialProvider(credential)

            MockCredentialProviderType.SIGNING_FAILURE ->
                FailingSignCredentialProvider(credential)

            MockCredentialProviderType.AUTH_CANCELLED_ONCE ->
                AuthCancelledOnceCredentialProvider(credential)
        }
}
