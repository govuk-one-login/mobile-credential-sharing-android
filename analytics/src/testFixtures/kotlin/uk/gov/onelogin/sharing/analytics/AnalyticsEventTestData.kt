package uk.gov.onelogin.sharing.analytics

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.ViewEvent

object AnalyticsEventTestData {
    val sampleLegacyScreenEvent = ViewEvent.Screen(
        name = "Unit test",
        id = "unitTest",
        params = RequiredParameters(
            taxonomyLevel2 = TaxonomyLevel2.WALLET
        )
    ).asLegacyEvent()
}
