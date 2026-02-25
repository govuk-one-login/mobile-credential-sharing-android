package uk.gov.onelogin.sharing.orchestration.verifier.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Verifying

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
