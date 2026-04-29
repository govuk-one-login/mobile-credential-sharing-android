package uk.gov.onelogin.sharing.testapp.credential.select

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import uk.gov.onelogin.sharing.testapp.credential.MockCredentialState

object SelectCredentialNavigationExt {
    fun NavController.navigateToHolderCredentialSelection(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        SelectCredentialRoute,
        options
    )

    internal fun NavGraphBuilder.configureSelectMockCredentialDialog(
        mockCredentials: List<MockCredentialState>,
        onSelectCredential: (MockCredentialState) -> Unit = {}
    ) {
        dialog<SelectCredentialRoute> {
            SelectCredentialsScreen(
                credentials = mockCredentials,
                onSelectCredential = onSelectCredential
            )
        }
    }
}
