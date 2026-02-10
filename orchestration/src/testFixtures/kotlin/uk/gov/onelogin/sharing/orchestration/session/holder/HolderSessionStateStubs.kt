package uk.gov.onelogin.sharing.orchestration.session.holder

import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorStubs.dummySessionError
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Preflight

object HolderSessionStateStubs {
    val userCancellation: HolderSessionState = Cancelled
    val userJourneyFailure: HolderSessionState = Failed(dummySessionError)
    val preflightEmptyPermissions: HolderSessionState = Preflight(setOf())
    val successStub: HolderSessionState = Success(DeviceResponse)
}
