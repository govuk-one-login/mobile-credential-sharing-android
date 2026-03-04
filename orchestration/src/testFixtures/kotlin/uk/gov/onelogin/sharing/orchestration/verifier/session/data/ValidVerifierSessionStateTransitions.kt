package uk.gov.onelogin.sharing.orchestration.verifier.session.data

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ProcessingEngagement
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.ReadyToScan
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Verifying
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionStateStubs.userJourneyFailure

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
            "Verifier session begins initialising" to preflightEmptyPermissions,
            "Verifier session is ready to scan" to ReadyToScan
        ).map { (testName, transition) ->
            Triple(
                testName,
                NotStarted,
                transition
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
        private val readyToScanTransitions = listOf(
            "User cancels whilst attempting to scan a QR code" to userCancellation,
            "Cannot obtain data from a scanned QR code" to userJourneyFailure,
            "Generated QR code gets shown to the User" to Connecting
        ).map { (testName, transition) ->
            Triple(
                testName,
                ReadyToScan,
                transition
            )
        }
        private val connectingTransitions = listOf(
            "User cancels whilst connecting with Holder device" to userCancellation,
            "Connection with Holder device cannot be established" to userJourneyFailure,
            "Receives Holder device's data transfer request" to ProcessingEngagement
        ).map { (testName, transition) ->
            Triple(
                testName,
                Connecting,
                transition
            )
        }
        private val processingEngagementTransitions = listOf(
            "User cancels whilst processing engagement" to userCancellation,
            "Cannot successfully process engagement" to userJourneyFailure,
            "Begins validating the shared digital credential" to Verifying
        ).map { (testName, transition) ->
            Triple(
                testName,
                ProcessingEngagement,
                transition
            )
        }
        private val verifyingTransitions = listOf(
            "User cancels the journey whilst validating the credential" to userCancellation,
            "Failure occurs when validating the credential" to userJourneyFailure,
            "User completes the Verifier User journey" to successStub
        ).map { (testName, transition) ->
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
