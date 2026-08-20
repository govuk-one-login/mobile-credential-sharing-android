package uk.gov.onelogin.sharing.testapp

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import java.io.InputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
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
        configureHolderJourneyWrapper { credential ->
            presentCredentialSdk
                .presenter(
                    SampleCredentialProvider(
                        credential
                    )
                )
        }
        configureVerifierAttributesSelection(navController)
        configureVerifierJourneyWrapper { context, verificationRequest ->
            val factory = CertificateFactory.getInstance("X.509")

            // Example: Reading from assets
            val stream: InputStream = context.assets.open("test_x509_certificate.der")
            val certificate: X509Certificate =
                factory.generateCertificate(stream) as X509Certificate
            verifyCredentialSdk.verifier(
                VerifierConfig(
                    verificationRequest = verificationRequest,
                    trustedRootCertificate = certificate
                )
            )
        }
    }
}
