package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs

class ValidVerifierSessionStateTransitions : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        inputs.mapIndexed { index, (testName, initial, transition) ->
            TestParameters.TestParametersValues.builder()
                .name("${index + 1}. $testName")
                .addParameter("initial", initial)
                .addParameter("transition", transition)
                .build()
        }

    companion object {
        private val preflightTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
            Triple(
                testName,
                VerifierSessionStateStubs.preflightEmptyPermissions,
                transition
            )
        }
        private val readyToScanTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
                    Triple(
                        testName,
                        VerifierSessionState.ReadyToScan,
                        transition
                    )
                }
        private val connectingTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
                    Triple(
                        testName,
                        VerifierSessionState.Connecting,
                        transition
                    )
                }
        private val processingEngagementTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
                    Triple(
                        testName,
                        VerifierSessionState.ProcessingEngagement,
                        transition
                    )
                }
        private val verifyingTransitions = emptyList<Pair<String, VerifierSessionState>>()
            .map { (testName, transition) ->
                Triple(
                    testName,
                    VerifierSessionState.Verifying,
                    transition
                )
            }

        val inputs: List<Triple<String, VerifierSessionState, VerifierSessionState>> = listOf(
            Triple(
                "Holder session begins initialising",
                VerifierSessionState.NotStarted,
                VerifierSessionStateStubs.preflightEmptyPermissions
            )
        ) + preflightTransitions +
                readyToScanTransitions +
                connectingTransitions +
                processingEngagementTransitions +
                verifyingTransitions
    }
}
