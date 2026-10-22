package uk.gov.onelogin.sharing.holder.presentation

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent

object HolderWelcomeViewModelTestData {
    private val expectedParameters = RequiredParameters(
        taxonomyLevel2 = TaxonomyLevel2.WALLET
    )

    val expectedScreenEvent = ViewEvent.Screen(
        name = "HolderWelcomeScreen",
        id = "29fd14a3-4390-4939-9d57-d9fe8cf61811",
        params = expectedParameters
    ).asLegacyEvent()
}
