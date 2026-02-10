package uk.gov.onelogin.sharing.orchestration.session.verifier

import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorStubs.dummySessionError
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Preflight

object VerifierSessionStateStubs {
    val userCancellation: VerifierSessionState = Cancelled
    val userJourneyFailure: VerifierSessionState = Failed(dummySessionError)
    val preflightEmptyPermissions: VerifierSessionState = Preflight(setOf())
    val successStub: VerifierSessionState = Success(DeviceResponse)
}
