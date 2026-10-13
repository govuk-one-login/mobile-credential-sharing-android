package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.orchestration.session.SessionErrorStubs
import uk.gov.onelogin.sharing.prerequisites.api.Actionable
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.Recoverable
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState

object HolderSessionStateStubs {
    val userCancellation: HolderSessionState = HolderSessionState.Complete.Cancelled
    val userJourneyFailure: HolderSessionState =
        HolderSessionState.Complete.Failed(SessionErrorStubs.dummySessionError)
    val preflightEmptyPermissions: HolderSessionState = HolderSessionState.Preflight(emptyList())
    fun preflightWithBluetooth(state: BluetoothState): HolderSessionState.Preflight =
        HolderSessionState.Preflight(
            listOf(
                // WET: rewriting MissingPrerequisites.Bluetooth data class to avoid new dependency
                object :
                MissingPrerequisite,
                Recoverable by state,
                Actionable<PrerequisiteAction> by state {
                    override val prerequisite: Prerequisite = Prerequisite.BLUETOOTH
                }
            )
        )
    val preflightWithoutBluetooth: HolderSessionState.Preflight = preflightWithBluetooth(
        BluetoothState.PoweredOff
    )
    val successStub: HolderSessionState = HolderSessionState.Complete.Success()
}
