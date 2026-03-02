package uk.gov.onelogin.sharing.orchestration.holder.session.data

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.userJourneyFailure

class ValidHolderSessionStateTransitions : TestParametersValuesProvider() {
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
            "Holder session begins initialising" to preflightEmptyPermissions
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.NotStarted,
                transition
            )
        }
        private val preflightTransitions = listOf(
            "User cancels during permission request" to userCancellation,
            "User permanently denies requested permissions" to userJourneyFailure,
            "User allows all requested permissions" to HolderSessionState.ReadyToPresent
        ).map { (testName, transition) ->
            Triple(
                testName,
                preflightEmptyPermissions,
                transition
            )
        }
        private val readyToPresentTransitions = listOf(
            "User cancels whilst generating QR code is shown" to userCancellation,
            "QR generation fails" to userJourneyFailure,
            "Generated QR code gets shown to the User" to
                HolderSessionState.PresentingEngagement("")
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.ReadyToPresent,
                transition
            )
        }
        private val presentingEngagementTransitions = listOf(
            "User cancels from the QR code screen" to userCancellation,
            "QR code handshake completes" to HolderSessionState.Connecting
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.PresentingEngagement(""),
                transition
            )
        }
        private val connectingTransitions = listOf(
            "User cancels whilst connecting with Verifier device" to userCancellation,
            "Connection with verifier device cannot be established" to userJourneyFailure,
            "Receives Verifier device's data transfer request" to HolderSessionState.RequestReceived
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.Connecting,
                transition
            )
        }
        private val requestReceivedTransitions = listOf(
            "User cancels the data transfer request" to userCancellation,
            "Data transfer disconnects before completion" to userJourneyFailure,
            "Holder device begins processing the response" to HolderSessionState.ProcessingResponse
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.RequestReceived,
                transition
            )
        }
        private val processingResponseTransitions = listOf(
            "User cancels the journey whilst validating the response" to userCancellation,
            "Failure occurs when validating the Verifier response" to userJourneyFailure,
            "User completes the Holder User journey" to successStub
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.ProcessingResponse,
                transition
            )
        }

        val inputs: List<Triple<String, HolderSessionState, HolderSessionState>> =
            notStartedTransitions +
                preflightTransitions +
                readyToPresentTransitions +
                presentingEngagementTransitions +
                connectingTransitions +
                requestReceivedTransitions +
                processingResponseTransitions
    }
}
