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
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.session.verifier.VerifierSessionStateStubs.userJourneyFailure

class InvalidVerifierSessionStateTransitions : TestParametersValuesProvider() {
    override fun provideValues(context: Context?): List<TestParameters.TestParametersValues?>? =
        inputs.mapIndexed { index, (initial, transition) ->
            TestParameters.TestParametersValues.builder()
                .name(
                    "${index + 1}. " +
                        "${initial::class.java.simpleName} -> " +
                        transition::class.java.simpleName
                )
                .addParameter("initial", initial)
                .addParameter("transition", transition)
                .build()
        }

    companion object {
        private val notStartedTransitions = listOf(
            NotStarted,
            ReadyToScan,
            Connecting,
            ProcessingEngagement,
            Verifying,
            successStub,
            userCancellation,
            userJourneyFailure
        ).map {
            NotStarted to it
        }

        private val preflightTransitions = listOf(
            NotStarted,
            preflightEmptyPermissions,
            Connecting,
            ProcessingEngagement,
            Verifying,
            successStub
        ).map {
            preflightEmptyPermissions to it
        }
        private val readyToScanTransitions = listOf(
            NotStarted,
            preflightEmptyPermissions,
            ReadyToScan,
            ProcessingEngagement,
            Verifying,
            successStub
        ).map {
            ReadyToScan to it
        }
        private val connectingTransitions = listOf(
            NotStarted,
            preflightEmptyPermissions,
            ReadyToScan,
            Connecting,
            Verifying,
            successStub
        ).map {
            Connecting to it
        }
        private val processingEngagementTransitions = listOf(
            NotStarted,
            preflightEmptyPermissions,
            ReadyToScan,
            Connecting,
            ProcessingEngagement,
            successStub
        ).map {
            ProcessingEngagement to it
        }
        private val VerifyingTransitions = listOf(
            NotStarted,
            preflightEmptyPermissions,
            ReadyToScan,
            Connecting,
            ProcessingEngagement
        ).map {
            Verifying to it
        }

        val inputs: List<Pair<VerifierSessionState, VerifierSessionState>> =
            notStartedTransitions +
                preflightTransitions +
                readyToScanTransitions +
                connectingTransitions +
                processingEngagementTransitions +
                VerifyingTransitions
    }
}
