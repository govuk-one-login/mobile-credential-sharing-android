package uk.gov.onelogin.sharing.orchestration.session.holder

import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Preflight

object HolderSessionStateStubs {
    val userCancellation = Cancelled
    val dummySessionError = SessionError(
        "This is a unit test",
        Exception()
    )
    val userJourneyFailure = Failed(dummySessionError)
    val preflightEmptyPermissions = Preflight(setOf())
    val successStub = Success(DeviceResponse)
}
