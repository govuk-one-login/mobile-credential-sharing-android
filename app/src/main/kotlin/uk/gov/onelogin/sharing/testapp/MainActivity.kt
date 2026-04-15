package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.api.BuildConfig
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialSdk

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var presentCredentialSdk: PresentCredentialSdk

    @Inject
    lateinit var verifyCredentialSdk: VerifyCredentialSdk

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mockCredentials = MockCredentials.getMockCredentials(this)

        if (BuildConfig.DEBUG) {
            Log.d(
                "Mock Credential",
                mockCredentials.toString()
            )
        }

        setContent {
            GdsTheme {
                TestAppScreen(
                    presentCredentialSdk = presentCredentialSdk,
                    mockCredentials = mockCredentials,
                    verifyCredentialSdk = verifyCredentialSdk
                )
            }
        }
    }
}
