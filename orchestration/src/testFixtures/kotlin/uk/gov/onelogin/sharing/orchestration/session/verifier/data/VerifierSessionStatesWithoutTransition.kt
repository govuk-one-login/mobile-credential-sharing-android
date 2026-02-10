package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs

class VerifierSessionStatesWithoutTransition : TestParameterValuesProvider() {
    override fun provideValues(context: Context?): List<*>? = inputs

    companion object {
        val inputs = listOf(
            VerifierSessionStateStubs.userCancellation,
            VerifierSessionStateStubs.userJourneyFailure,
            VerifierSessionStateStubs.successStub
        )
    }
}
