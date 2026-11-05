package uk.gov.onelogin.sharing.orchestration.holder.session

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
    val successStub: HolderSessionState = HolderSessionState.Complete.Success()
}
