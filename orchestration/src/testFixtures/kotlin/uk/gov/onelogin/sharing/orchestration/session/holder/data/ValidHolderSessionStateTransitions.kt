package uk.gov.onelogin.sharing.orchestration.session.holder.data

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs

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
        private val preflightTransitions = listOf(
            "User cancels during permission request" to HolderSessionStateStubs.userCancellation,
            "User permanently denies requested permissions" to
                HolderSessionStateStubs.userJourneyFailure,
            "User allows all requested permissions" to HolderSessionState.ReadyToPresent
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionStateStubs.preflightEmptyPermissions,
                transition
            )
        }
        private val readyToPresentTransitions = listOf(
            "User cancels whilst generating QR code is shown" to
                HolderSessionStateStubs.userCancellation,
            "QR generation fails" to HolderSessionStateStubs.userJourneyFailure,
            "Generated QR code gets shown to the User" to HolderSessionState.PresentingEngagement
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.ReadyToPresent,
                transition
            )
        }
        private val presentingEngagementTransitions = listOf(
            "User cancels from the QR code screen" to HolderSessionStateStubs.userCancellation,
            "QR code handshake completes" to HolderSessionState.Connecting
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.PresentingEngagement,
                transition
            )
        }
        private val connectingTransitions = listOf(
            "User cancels whilst connecting with Verifier device" to
                HolderSessionStateStubs.userCancellation,
            "Connection with verifier device cannot be established" to
                HolderSessionStateStubs.userJourneyFailure,
            "Receives Verifier device's data transfer request" to HolderSessionState.RequestReceived
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.Connecting,
                transition
            )
        }
        private val requestReceivedTransitions = listOf(
            "User cancels the data transfer request" to HolderSessionStateStubs.userCancellation,
            "Data transfer disconnects before completion" to
                HolderSessionStateStubs.userJourneyFailure,
            "Holder device begins processing the response" to HolderSessionState.ProcessingResponse
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.RequestReceived,
                transition
            )
        }
        private val processingResponseTransitions = listOf(
            "User cancels the journey whilst validating the response" to
                HolderSessionStateStubs.userCancellation,
            "Failure occurs when validating the Verifier response" to
                HolderSessionStateStubs.userJourneyFailure,
            "User completes the Holder User journey" to HolderSessionStateStubs.successStub
        ).map { (testName, transition) ->
            Triple(
                testName,
                HolderSessionState.ProcessingResponse,
                transition
            )
        }

        val inputs: List<Triple<String, HolderSessionState, HolderSessionState>> = listOf(
            Triple(
                "Holder session begins initialising",
                HolderSessionState.NotStarted,
                HolderSessionStateStubs.preflightEmptyPermissions
            )
        ) + preflightTransitions +
            readyToPresentTransitions +
            presentingEngagementTransitions +
            connectingTransitions +
            requestReceivedTransitions +
            processingResponseTransitions
    }
}
