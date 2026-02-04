package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.userJourneyFailure

class HolderSessionStatesWithoutTransition : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs = listOf(
            userCancellation,
            userJourneyFailure,
            successStub,
        )
    }
}
