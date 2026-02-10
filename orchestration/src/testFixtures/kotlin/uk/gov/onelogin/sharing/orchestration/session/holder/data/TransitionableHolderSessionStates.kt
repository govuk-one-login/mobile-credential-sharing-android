package uk.gov.onelogin.sharing.orchestration.session.holder.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.RequestReceived

class TransitionableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs: List<KClass<out HolderSessionState>> = listOf(
            NotStarted::class,
            Preflight::class,
            ReadyToPresent::class,
            PresentingEngagement::class,
            Connecting::class,
            RequestReceived::class,
            ProcessingResponse::class
        )
    }
}
