package uk.gov.onelogin.sharing.orchestration.holder.session

import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorStubs

object HolderSessionStateStubs {
    val userCancellation: HolderSessionState = HolderSessionState.Complete.Cancelled
    val userJourneyFailure: HolderSessionState =
        HolderSessionState.Complete.Failed(SessionErrorStubs.dummySessionError)
    val preflightEmptyPermissions: HolderSessionState = HolderSessionState.Preflight(emptyList())
    val successStub: HolderSessionState = HolderSessionState.Complete.Success(DeviceResponse)
}
