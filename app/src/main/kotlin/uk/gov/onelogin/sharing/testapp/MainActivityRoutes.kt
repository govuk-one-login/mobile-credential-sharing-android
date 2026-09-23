package uk.gov.onelogin.sharing.testapp

import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import java.io.IOException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingSdk
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

@Suppress("LongParameterList")
object MainActivityRoutes {
    internal fun NavGraphBuilder.configureTestAppRoutes(
        context: Context,
        mockCredentials: List<MockCredentialState>,
        navController: NavController,
        sharingSdkImpl: CredentialSharingSdk,
        verifyCredentialSdk: VerifyCredentialSdk,
        issuerRootCertificateProvider: IssuerRootCertificateProvider
    ) {
        configureTestAppHomeScreen(navController)
        configureSelectMockCredentialDialog(
            controller = navController,
            mockCredentials = mockCredentials
        )
        configureHolderJourneyWrapper { credential ->
            sharingSdkImpl.createCredentialPresenter(
                credentialProvider = credentialProviderFor(credential),
                trustedReaderCertificates = loadReaderRootCertificate(context)
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

    private fun loadReaderRootCertificate(context: Context): List<X509Certificate> = try {
        context.assets.open("test_reader_auth_x509_certificate.der").use { inputStream ->
            val cert = CertificateFactory.getInstance("X.509")
                .generateCertificate(inputStream) as X509Certificate
            listOf(cert)
        }
    } catch (_: IOException) {
        emptyList()
    } catch (_: CertificateException) {
        emptyList()
    }
}
