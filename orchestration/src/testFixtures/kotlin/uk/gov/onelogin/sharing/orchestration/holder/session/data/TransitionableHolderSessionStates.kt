package uk.gov.onelogin.sharing.orchestration.holder.session.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import kotlin.reflect.KClass
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Connected
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState.RequestReceived

class TransitionableHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs: List<KClass<out HolderSessionState>> = listOf(
            NotStarted::class,
            Preflight::class,
            ReadyToPresent::class,
            PresentingEngagement::class,
            Connected::class,
            RequestReceived::class,
            ProcessingResponse::class
        )
    }
}
