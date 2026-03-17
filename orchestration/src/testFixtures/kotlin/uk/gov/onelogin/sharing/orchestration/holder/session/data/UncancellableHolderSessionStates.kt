package uk.gov.onelogin.sharing.orchestration.holder.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

/**
 * Parameterised test input for [HolderSessionState] objects that can't transition to
 * [HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.sharing.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.holder.session.validHolderTransitions] [Map].
 */
class UncancellableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = listOf(
        HolderSessionState.NotStarted,
        HolderSessionStateStubs.successStub,
        HolderSessionStateStubs.userCancellation,
        HolderSessionStateStubs.userJourneyFailure
    )
}
