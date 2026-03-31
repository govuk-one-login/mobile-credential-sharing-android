package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import java.security.cert.Certificate
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.BuildConfig
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.orchestration.verificationrequest.DocumentType
import uk.gov.onelogin.sharing.orchestration.verificationrequest.MdlAttribute
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        private const val AGE_18 = 18
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
            attributeGroup = AttributeGroup(
                mapOf(
                    MdlAttribute.GivenName to true,
                    MdlAttribute.FamilyName to true,
                    MdlAttribute.AgeOver(AGE_18) to false
                )
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

        if (BuildConfig.DEBUG) {
            Log.d(
                "Mock Credential",
                MockCredentials.mockCredential(this).toString()
            )
        }

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
