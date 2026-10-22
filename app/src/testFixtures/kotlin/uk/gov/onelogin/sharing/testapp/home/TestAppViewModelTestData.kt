package uk.gov.onelogin.sharing.testapp.home

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object TestAppViewModelTestData {
    val expectedScreenEvent = Screen(
        name = "Credential Sharing Test App",
        id = "TestAppScreen",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
