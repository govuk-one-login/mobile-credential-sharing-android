package uk.gov.onelogin.sharing.orchestration.verifier.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.userJourneyFailure

/**
 * Parameterised test input for [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState] objects that can't transition to
 * [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.holder.session.validHolderTransitions] [Map].
 */
class UncancellableVerifierSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = listOf(
        NotStarted,
        successStub,
        userCancellation,
        userJourneyFailure
    )
}
