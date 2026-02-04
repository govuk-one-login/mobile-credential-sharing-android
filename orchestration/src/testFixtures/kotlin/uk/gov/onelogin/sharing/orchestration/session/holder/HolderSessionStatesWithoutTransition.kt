package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider

class HolderSessionStatesWithoutTransition : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs = listOf(
            HolderSessionState.Complete.Cancelled::class,
            HolderSessionState.Complete.Failed::class,
            HolderSessionState.Complete.Success::class
        )
    }
}
