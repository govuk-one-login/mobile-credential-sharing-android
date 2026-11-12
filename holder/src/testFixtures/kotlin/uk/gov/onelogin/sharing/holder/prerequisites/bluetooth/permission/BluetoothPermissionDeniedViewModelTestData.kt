package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters

object BluetoothPermissionDeniedViewModelTestData {
    const val SCREEN_NAME = "You need to allow connection to nearby devices"
    const val SCREEN_ID = "4644246f-fb12-456e-8162-7f7d2a856b50"
    const val ALLOW_BUTTON_TEXT = "Allow"
    const val CANCEL_BUTTON_TEXT = "Cancel and go back to documents"

    val expectedScreenEvent
        get() = Screen(
            name = SCREEN_NAME,
            id = SCREEN_ID,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedAllowEvent
        get() = Button(
            text = ALLOW_BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()

    val expectedCancelEvent
        get() = Button(
            text = CANCEL_BUTTON_TEXT,
            params = walletSharingRequiredParameters
        ).asLegacyEvent()
}
