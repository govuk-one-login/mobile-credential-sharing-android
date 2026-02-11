package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.userJourneyFailure

/**
 * Parameterised test input for [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState] objects that can't transition to
 * [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.session.holder.validHolderTransitions] [Map].
 */
class UncancellableVerifierSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = listOf(
        NotStarted,
        successStub,
        userCancellation,
        userJourneyFailure
    )
}
