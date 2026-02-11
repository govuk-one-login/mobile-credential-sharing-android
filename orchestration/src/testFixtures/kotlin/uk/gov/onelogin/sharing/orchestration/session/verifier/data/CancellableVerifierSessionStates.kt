package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Verifying
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.preflightEmptyPermissions

/**
 * Parameterised test input for valid [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState] objects that can transition to
 * [uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled] within the
 * [uk.gov.onelogin.orchestration.HolderOrchestrator], as per the [uk.gov.onelogin.sharing.orchestration.session.holder.validHolderTransitions] [Map].
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
