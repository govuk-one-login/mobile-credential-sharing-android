package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider

class ValidHolderSessionStateTransitions : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        listOf<Triple<String, HolderSessionState, HolderSessionState>>(
            Triple(
                "Holder session begins",
                HolderSessionState.NotStarted,
                HolderSessionState.Initialising,
            )
        ).map { (testName, initial, transition) ->
            TestParameters.TestParametersValues.builder()
                .name(testName)
                .addParameter("initial", initial)
                .addParameter("transition", transition)
                .build()
        }
}
