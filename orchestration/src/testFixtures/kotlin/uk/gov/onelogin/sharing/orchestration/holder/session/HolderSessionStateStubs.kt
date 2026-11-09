package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorStubs
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites

object HolderSessionStateStubs {
    val userCancellation: HolderSessionState = HolderSessionState.Complete.Cancelled
    val userJourneyFailure: HolderSessionState =
        HolderSessionState.Complete.Failed(SessionErrorStubs.dummySessionError)
    val preflightEmptyPermissions: HolderSessionState = HolderSessionState.Preflight(emptyList())
    fun preflightWithBluetooth(state: BluetoothState) = HolderSessionState.Preflight(
        missingPrerequisites = listOf(MissingPrerequisites.Bluetooth(state))
    )
    val preflightWithoutBluetooth: HolderSessionState.Preflight = preflightWithBluetooth(
        BluetoothState.PoweredOff
    )
    val restrictedBluetoothFailure: HolderSessionState = HolderSessionState.Complete.Failed(
        SessionError(
            "Device cannot perform journey",
            SessionErrorReason.UnrecoverablePrerequisite(
                MissingPrerequisites.Bluetooth(BluetoothState.Restricted)
            )
        )
    )
    val successStub: HolderSessionState = HolderSessionState.Complete.Success()

    val consentPresentationStub: ConsentPresentation = ConsentPresentation(
        documents = emptyList(),
        organizationName = "Example Organisation",
        privacyPolicyUrl = "https://gov.uk/"
    )

    val awaitingUserConsentStub: HolderSessionState = HolderSessionState.AwaitingUserConsent(
        request = deviceRequestStub,
        presentation = consentPresentationStub
    )
}
