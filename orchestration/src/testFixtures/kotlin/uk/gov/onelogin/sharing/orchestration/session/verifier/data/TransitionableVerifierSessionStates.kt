package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState

class TransitionableVerifierSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs: List<KClass<out VerifierSessionState>> = listOf(
            VerifierSessionState.NotStarted::class,
        )
    }
}
