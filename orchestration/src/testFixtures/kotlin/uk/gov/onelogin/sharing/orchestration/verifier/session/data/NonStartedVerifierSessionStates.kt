package uk.gov.onelogin.sharing.orchestration.verifier.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Verifying
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs

/**
 * Parameterised test input for [VerifierSessionState]
 * objects that are not [VerifierSessionState.NotStarted].
 * Includes both in-progress and complete states.
 */
class NonStartedVerifierSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*> = listOf(
        ReadyToScan,
        Connecting,
        Verifying,
        VerifierSessionStateStubs.userCancellation,
        VerifierSessionStateStubs.userJourneyFailure,
        VerifierSessionStateStubs.successStub
    )
}
