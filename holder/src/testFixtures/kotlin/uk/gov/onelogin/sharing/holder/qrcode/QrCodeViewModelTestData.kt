package uk.gov.onelogin.sharing.holder.qrcode

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object QrCodeViewModelTestData {
    const val SCREEN_NAME = "Find out which details you need to share"

    val expectedScreenEvent = ViewEvent.Screen(
        name = SCREEN_NAME,
        id = "29fd14a3-4390-4939-9d57-d9fe8cf61811",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
