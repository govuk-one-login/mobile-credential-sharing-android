package uk.gov.onelogin.sharing.testapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var credentialSharingSdk: CredentialSharingSdk

    @Inject
    lateinit var credentialSharingUi: CredentialSharingUi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GdsTheme {
                TestAppScreen(
                    ui = credentialSharingUi,
                    sdk = credentialSharingSdk
                )
            }
        }
    }
}
