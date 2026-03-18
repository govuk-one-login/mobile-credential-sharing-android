package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import java.security.cert.Certificate
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.RequestElement
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val AGE_21 = 21
    }

    @Inject
    lateinit var presentCredentialSdk: PresentCredentialSdk

    @Inject
    lateinit var verifyCredentialSdk: VerifyCredentialSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val holder = presentCredentialSdk
            .presenter(SampleCredentialProvider())

        val verificationRequest = VerificationRequest.typed(
            documentType = DocumentType.Mdl,
            requestElements = listOf(
                RequestElement.GivenName,
                RequestElement.AgeOver(AGE_21),
                RequestElement.FamilyName,
                RequestElement.Portrait
            )
        )
        val trustedCertificates: List<Certificate> = emptyList()

        val verifier = verifyCredentialSdk
            .verifier(
                VerifierConfig(
                    verificationRequest = verificationRequest,
                    trustedCertificates = trustedCertificates
                )
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
