package uk.gov.onelogin.sharing.orchestration.session.holder.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs

/**
 * Parameterised test input for valid [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState] objects that can transition to
 * [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.session.holder.validHolderTransitions] [Map].
 */
class CancellableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = listOf(
        HolderSessionStateStubs.preflightEmptyPermissions,
        HolderSessionState.ReadyToPresent,
        HolderSessionState.PresentingEngagement,
        HolderSessionState.Connecting,
        HolderSessionState.RequestReceived,
        HolderSessionState.ProcessingResponse
    )
}
