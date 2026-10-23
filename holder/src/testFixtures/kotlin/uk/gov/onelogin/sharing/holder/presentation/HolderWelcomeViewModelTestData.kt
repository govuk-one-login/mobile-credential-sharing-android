package uk.gov.onelogin.sharing.holder.presentation

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object HolderWelcomeViewModelTestData {
    val expectedScreenEvent = ViewEvent.Screen(
        name = "HolderWelcomeScreen",
        id = "29fd14a3-4390-4939-9d57-d9fe8cf61811",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
