package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import java.security.cert.Certificate
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.verifier.VerificationRequest
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialSdk

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var presenterCredentialSdk: PresenterCredentialSdk

    @Inject
    lateinit var verifierCredentialSdk: VerifierCredentialSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val holder = presenterCredentialSdk
            .presenter(SampleCredentialProvider())

        val verificationRequest = VerificationRequest(
            documentType = "org.iso.18013.5.1.mDL",
            requestedElements = listOf("given_name", "age_over_21", "family_name", "portrait")
        )
        val trustedCertificates: List<Certificate> = emptyList()

        val verifier = verifierCredentialSdk
            .verifier(
                verificationRequest = verificationRequest,
                trustedCertificates = trustedCertificates
            )

        setContent {
            GdsTheme {
                TestAppScreen(
                    credentialPresenter = holder,
                    credentialVerifier = verifier
                )
            }
        }
    }
}
