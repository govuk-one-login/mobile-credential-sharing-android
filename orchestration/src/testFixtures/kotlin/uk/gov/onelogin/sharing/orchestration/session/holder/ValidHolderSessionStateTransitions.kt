package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.DeviceResponse
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Cancelled
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Complete.Success
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Initialising
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Preflight
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.RequestReceived

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
        private val userCancellation = Cancelled
        private val dummySessionError = SessionError(
            "This isn't used in HolderSessionState tests",
            Exception(),
        )
        private val userJourneyFailure = Failed(dummySessionError)
        private val preflightEmptyPermissions = Preflight(setOf())
        private val preflightTransitions = listOf(
            "User cancels during permission request" to userCancellation,
            "User permanently denies requested permissions" to userJourneyFailure,
            "User allows all requested permissions" to ReadyToPresent,
        ).map { (testName, transition) ->
            Triple(
                testName,
                preflightEmptyPermissions,
                transition,
            )
        }
        private val readyToPresentTransitions = listOf(
            "User cancels whilst generating QR code is shown" to userCancellation,
            "QR generation fails" to userJourneyFailure,
            "Generated QR code gets shown to the User" to PresentingEngagement,
        ).map { (testName, transition) ->
            Triple(
                testName,
                ReadyToPresent,
                transition,
            )
        }
        private val presentingEngagementTransitions = listOf(
            "User cancels from the QR code screen" to userCancellation,
            "QR code handshake completes" to Connecting,
        ).map { (testName, transition) ->
            Triple(
                testName,
                PresentingEngagement,
                transition,
            )
        }
        private val connectingTransitions = listOf(
            "User cancels whilst connecting with Verifier device" to userCancellation,
            "Connection with verifier device cannot be established" to userJourneyFailure,
            "Receives Verifier device's data transfer request" to RequestReceived,
        ).map { (testName, transition) ->
            Triple(
                testName,
                Connecting,
                transition,
            )
        }
        private val requestReceivedTransitions = listOf(
            "User cancels the data transfer request" to userCancellation,
            "Data transfer disconnects before completion" to userJourneyFailure,
            "Holder device begins processing the response" to ProcessingResponse,
        ).map { (testName, transition) ->
            Triple(
                testName,
                RequestReceived,
                transition,
            )
        }
        private val processingResponseTransitions = listOf(
            "User cancels the journey whilst validating the response" to userCancellation,
            "Failure occurs when validating the Verifier response" to userJourneyFailure,
            "User completes the Holder User journey" to Success(DeviceResponse),
        ).map { (testName, transition) ->
            Triple(
                testName,
                ProcessingResponse,
                transition,
            )
        }

        val inputs: List<Triple<String, HolderSessionState, HolderSessionState>> = listOf(
            Triple(
                "Holder session begins initialising",
                NotStarted,
                Initialising,
            ),
            Triple(
                "Holder session requests permissions",
                Initialising,
                preflightEmptyPermissions,
            ),
        ) + preflightTransitions +
                readyToPresentTransitions +
                presentingEngagementTransitions +
                connectingTransitions +
                requestReceivedTransitions +
                processingResponseTransitions
    }
}
