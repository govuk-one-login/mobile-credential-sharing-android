package uk.gov.onelogin.sharing.testapp.home

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen

object TestAppViewModelTestData {
    val expectedScreenEvent = Screen(
        name = "Credential Sharing Test App",
        id = "TestAppScreen",
        params = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.WALLET
        )
    ).asLegacyEvent()
}
