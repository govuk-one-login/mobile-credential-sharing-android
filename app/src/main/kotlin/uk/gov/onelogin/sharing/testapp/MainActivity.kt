package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.sdk.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.VerificationRequest
import uk.gov.onelogin.sharing.ui.impl.CredentialPresenterImpl
import uk.gov.onelogin.sharing.ui.impl.CredentialVerifierImpl

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var credentialSharingSdk: CredentialSharingSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Holder role API
        val credentialProvider = SampleCredentialProvider()
        val credentialPresenter = CredentialPresenterImpl(
            credentialProvider,
            credentialSharingSdk.appGraph
        )

        // Initialize Verifier role API
        val credentialVerifier = CredentialVerifierImpl(
            verificationRequest = VerificationRequest(
                documentType = "org.iso.18013.5.1.mDL",
                requestedElements = listOf("given_name", "age_over_21", "family_name", "portrait")
            ),
            trustedCertificates = emptyList(), // Would load trusted CAs in production
            appGraph = credentialSharingSdk.appGraph
        )

        setContent {
            GdsTheme {
                TestAppScreen(
                    credentialPresenter = credentialPresenter,
                    credentialVerifier = credentialVerifier
                )
            }
        }
    }
}
