package uk.gov.onelogin.sharing.testapp.verifier

import android.R.drawable.ic_menu_close_clear_cancel
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier
import uk.gov.onelogin.sharing.ui.impl.VerifyCredential

@Composable
internal fun VerifierTestAppJourneyScreen(
    verifier: CredentialVerifier,
    modifier: Modifier = Modifier,
    onCloseJourney: () -> Unit = {}
) {
    Surface(modifier = modifier) {
        VerifyCredential(
            component = verifier,
            modifier = Modifier.fillMaxSize()
        )

        Box {
            IconButton(
                modifier = Modifier.align(Alignment.TopStart),
                onClick = onCloseJourney
            ) {
                Icon(
                    painter = painterResource(ic_menu_close_clear_cancel),
                    contentDescription = "Close"
                )
            }
        }
    }
}
