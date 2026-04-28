package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.dialog
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup

object SelectCredentialAttributesNavigationExt {

    fun NavController.navigateToVerifierAttributesSelection(
        options: NavOptionsBuilder.() -> Unit = {}
    ) = navigate(
        SelectCredentialAttributesRoute,
        options
    )

    internal fun NavGraphBuilder.configureVerifierAttributesSelection(
        onSelectAttributeGroup: (AttributeGroup) -> Unit = {}
    ) {
        dialog<SelectCredentialAttributesRoute> {
            SelectCredentialAttributesScreen(
                onSelectAttributeGroup = onSelectAttributeGroup
            )
        }
    }
}
