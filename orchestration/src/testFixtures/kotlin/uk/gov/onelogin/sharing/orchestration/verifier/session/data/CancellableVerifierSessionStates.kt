package uk.gov.onelogin.sharing.orchestration.verifier.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Verifying
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions

/**
 * Parameterised test input for valid [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState] objects that can transition to
 * [uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.sharing.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.holder.session.validHolderTransitions] [Map].
 */
class CancellableVerifierSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = listOf(
        preflightEmptyPermissions,
        ReadyToScan,
        Connecting,
        ProcessingEngagement,
        Verifying
    )
}
