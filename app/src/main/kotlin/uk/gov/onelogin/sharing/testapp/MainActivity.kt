package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.orchestration.CredentialProviderNewImpl
import uk.gov.onelogin.sharing.CredentialSharingSdk

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var credentialSharingSdk: CredentialSharingSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // this can be injected via Hilt like the credentialSharingSdk
        val holder = credentialSharingSdk
            .presenterCredentialSdk
            .presenter(CredentialProviderNewImpl())

        val verifier = credentialSharingSdk
            .verifierCredentialSdk
            .verifier()

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
