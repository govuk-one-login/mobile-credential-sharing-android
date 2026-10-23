package uk.gov.onelogin.sharing.holder.getreadytoshare

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object GetReadyToShareTestData {
    val expectedScreenEvent = ViewEvent.Screen(
        name = "Get ready to share",
        id = "3f5c1d0b-8068-4e19-8300-37f7bb468ff5",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
