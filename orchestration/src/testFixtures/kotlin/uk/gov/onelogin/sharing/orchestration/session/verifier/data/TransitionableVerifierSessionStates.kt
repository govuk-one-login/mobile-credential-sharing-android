package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Verifying

class TransitionableVerifierSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs: List<KClass<out VerifierSessionState>> = listOf(
            NotStarted::class,
            Preflight::class,
            ReadyToScan::class,
            Connecting::class,
            ProcessingEngagement::class,
            Verifying::class
        )
    }
}
