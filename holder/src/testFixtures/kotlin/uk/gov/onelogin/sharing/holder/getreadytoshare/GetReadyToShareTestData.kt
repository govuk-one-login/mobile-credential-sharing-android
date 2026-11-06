package uk.gov.onelogin.sharing.holder.getreadytoshare

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object GetReadyToShareTestData {
    val expectedScreenEvent = Screen(
        name = "Get ready to share",
        id = "3f5c1d0b-8068-4e19-8300-37f7bb468ff5",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    val expectedAllowBluetoothButtonEvent = Button(
        text = "Allow bluetooth",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    val expectedFindOutMoreButtonEvent = Button(
        text = "Find out how sharing works",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()
}
