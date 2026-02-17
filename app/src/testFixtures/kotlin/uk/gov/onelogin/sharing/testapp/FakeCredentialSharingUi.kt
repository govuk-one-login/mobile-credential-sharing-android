package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.api.CredentialSharingUi

class FakeCredentialSharingUi : CredentialSharingUi {
    @Composable
    override fun Render(
        sdk: CredentialSharingSdk,
        startDestination: CredentialSharingDestination,
        modifier: Modifier
    ) {
    }
}
