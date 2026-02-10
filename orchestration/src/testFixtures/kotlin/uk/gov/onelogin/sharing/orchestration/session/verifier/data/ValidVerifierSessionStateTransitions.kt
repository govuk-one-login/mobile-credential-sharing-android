package uk.gov.onelogin.sharing.orchestration.session.verifier.data

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionState.Verifying
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.userJourneyFailure

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
        private val notStartedTransitions = listOf(
            "Holder session begins initialising" to preflightEmptyPermissions,
        ).map { (testName, transition) ->
            Triple(
                testName,
                NotStarted,
                transition,
            )
        }
        private val preflightTransitions = listOf(
            "User cancels during permission request" to userCancellation,
            "User permanently denies requested permissions" to userJourneyFailure,
            "User allows all requested permissions" to ReadyToScan
        ).map { (testName, transition) ->
                    Triple(
                        testName,
                        preflightEmptyPermissions,
                        transition
                    )
                }
        private val readyToScanTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
                    Triple(
                        testName,
                        ReadyToScan,
                        transition
                    )
                }
        private val connectingTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
                    Triple(
                        testName,
                        Connecting,
                        transition
                    )
                }
        private val processingEngagementTransitions =
            emptyList<Pair<String, VerifierSessionState>>()
                .map { (testName, transition) ->
                    Triple(
                        testName,
                        ProcessingEngagement,
                        transition
                    )
                }
        private val verifyingTransitions = emptyList<Pair<String, VerifierSessionState>>()
            .map { (testName, transition) ->
                Triple(
                    testName,
                    Verifying,
                    transition
                )
            }

        val inputs: List<Triple<String, VerifierSessionState, VerifierSessionState>> =
            notStartedTransitions +
                    preflightTransitions +
                    readyToScanTransitions +
                    connectingTransitions +
                    processingEngagementTransitions +
                    verifyingTransitions
    }
}
