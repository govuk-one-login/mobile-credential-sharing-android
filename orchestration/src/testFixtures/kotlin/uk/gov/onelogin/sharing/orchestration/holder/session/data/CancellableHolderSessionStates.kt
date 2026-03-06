package uk.gov.onelogin.sharing.orchestration.holder.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs
import uk.gov.onelogin.sharing.security.DeviceRequestStub.deviceRequestStub

/**
 * Parameterised test input for valid [HolderSessionState] objects that can transition to
 * [HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.holder.session.validHolderTransitions] [Map].
 */
class CancellableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*> = listOf(
        HolderSessionStateStubs.preflightEmptyPermissions,
        HolderSessionState.ReadyToPresent,
        HolderSessionState.PresentingEngagement(""),
        HolderSessionState.ProcessingEstablishment,
        HolderSessionState.AwaitingUserConsent(deviceRequestStub),
        HolderSessionState.ProcessingResponse
    )
}
