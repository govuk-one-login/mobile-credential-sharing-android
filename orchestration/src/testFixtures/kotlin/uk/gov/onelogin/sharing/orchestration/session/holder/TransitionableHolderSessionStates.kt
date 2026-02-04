package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.reflect.KClass

class TransitionableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs: List<KClass<out HolderSessionState>> = listOf(
            HolderSessionState.NotStarted::class,
            HolderSessionState.Initialising::class,
            HolderSessionState.Preflight::class,
            HolderSessionState.ReadyToPresent::class,
            HolderSessionState.PresentingEngagement::class,
            HolderSessionState.Connecting::class,
            HolderSessionState.RequestReceived::class,
            HolderSessionState.ProcessingResponse::class
        )
    }
}
