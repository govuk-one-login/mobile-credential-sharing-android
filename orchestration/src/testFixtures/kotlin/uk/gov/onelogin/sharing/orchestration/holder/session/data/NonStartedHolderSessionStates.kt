package uk.gov.onelogin.sharing.orchestration.holder.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

/**
 * Parameterised test input for [HolderSessionState] objects that are
 * not [HolderSessionState.NotStarted].
 * Includes both in-progress and complete states.
 */
class NonStartedHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*> = listOf(
        HolderSessionState.PresentingEngagement(""),
        HolderSessionState.AwaitingUserConsent(deviceRequestStub),
        HolderSessionState.AwaitingVerifierResolution,
        HolderSessionStateStubs.userCancellation,
        HolderSessionStateStubs.userJourneyFailure,
        HolderSessionStateStubs.successStub
    )
}
