package uk.gov.onelogin.sharing.ui.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.gov.onelogin.sharing.CredentialSharingSdk

interface CredentialSharingUi {
    @Composable
    fun Render(
        sdk: CredentialSharingSdk,
        startDestination: CredentialSharingDestination,
        modifier: Modifier = Modifier
    )
}
