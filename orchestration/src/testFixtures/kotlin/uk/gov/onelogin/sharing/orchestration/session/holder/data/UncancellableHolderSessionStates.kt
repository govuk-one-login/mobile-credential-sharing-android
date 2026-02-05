package uk.gov.onelogin.sharing.orchestration.session.holder.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs

/**
 * Parameterised test input for [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState] objects that can't transition to
 * [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.session.holder.validHolderTransitions] [Map].
 */
class UncancellableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = listOf(
        HolderSessionState.NotStarted,
        HolderSessionStateStubs.successStub,
        HolderSessionStateStubs.userCancellation,
        HolderSessionStateStubs.userJourneyFailure
    )
}
