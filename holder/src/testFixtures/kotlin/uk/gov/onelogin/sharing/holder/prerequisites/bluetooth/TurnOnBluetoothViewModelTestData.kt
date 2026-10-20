package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.RequiredParameters
import uk.gov.logging.api.v3dot1.model.TrackEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent

object TurnOnBluetoothViewModelTestData {
    private val expectedParams = RequiredParameters(
        taxonomyLevel2 = TaxonomyLevel2.WALLET
    )

    val expectedScreenEvent = ViewEvent.Screen(
        name = "Turn on Bluetooth",
        id = "d64962b9-def5-4b13-9c3c-128d51353fe7",
        params = expectedParams
    ).asLegacyEvent()

    val expectedTurnOnBluetoothEvent = TrackEvent.Button(
        text = "Turn on Bluetooth",
        params = expectedParams
    ).asLegacyEvent()
}
