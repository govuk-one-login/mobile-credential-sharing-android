package uk.gov.onelogin.sharing.orchestration.session.holder.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs

class CompleteHolderSessionStates : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs = listOf(
            HolderSessionStateStubs.userCancellation,
            HolderSessionStateStubs.userJourneyFailure,
            HolderSessionStateStubs.successStub
        )
    }
}
