package uk.gov.onelogin.sharing.orchestration.holder.session.data

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

class InvalidHolderSessionStateTransitions : TestParametersValuesProvider() {
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
            HolderSessionState.NotStarted,
            HolderSessionState.ReadyToPresent,
            HolderSessionState.PresentingEngagement(""),
            HolderSessionState.Connecting,
            HolderSessionState.RequestReceived,
            HolderSessionState.ProcessingResponse,
            HolderSessionStateStubs.successStub,
            HolderSessionStateStubs.userCancellation,
            HolderSessionStateStubs.userJourneyFailure
        ).map {
            HolderSessionState.NotStarted to it
        }

        private val preflightTransitions = listOf(
            HolderSessionState.NotStarted,
            HolderSessionStateStubs.preflightEmptyPermissions,
            HolderSessionState.PresentingEngagement(""),
            HolderSessionState.Connecting,
            HolderSessionState.RequestReceived,
            HolderSessionState.ProcessingResponse,
            HolderSessionStateStubs.successStub
        ).map {
            HolderSessionStateStubs.preflightEmptyPermissions to it
        }
        private val readyToPresentTransitions = listOf(
            HolderSessionState.NotStarted,
            HolderSessionStateStubs.preflightEmptyPermissions,
            HolderSessionState.ReadyToPresent,
            HolderSessionState.Connecting,
            HolderSessionState.RequestReceived,
            HolderSessionState.ProcessingResponse,
            HolderSessionStateStubs.successStub
        ).map {
            HolderSessionState.ReadyToPresent to it
        }
        private val presentingEngagementTransitions = listOf(
            HolderSessionState.NotStarted,
            HolderSessionStateStubs.preflightEmptyPermissions,
            HolderSessionState.ReadyToPresent,
            HolderSessionState.PresentingEngagement(""),
            HolderSessionState.RequestReceived,
            HolderSessionState.ProcessingResponse,
            HolderSessionStateStubs.successStub,
            HolderSessionStateStubs.userJourneyFailure
        ).map {
            HolderSessionState.PresentingEngagement("") to it
        }
        private val connectingTransitions = listOf(
            HolderSessionState.NotStarted,
            HolderSessionStateStubs.preflightEmptyPermissions,
            HolderSessionState.ReadyToPresent,
            HolderSessionState.PresentingEngagement(""),
            HolderSessionState.Connecting,
            HolderSessionState.ProcessingResponse,
            HolderSessionStateStubs.successStub
        ).map {
            HolderSessionState.Connecting to it
        }
        private val requestReceivedTransitions = listOf(
            HolderSessionState.NotStarted,
            HolderSessionStateStubs.preflightEmptyPermissions,
            HolderSessionState.ReadyToPresent,
            HolderSessionState.PresentingEngagement(""),
            HolderSessionState.Connecting,
            HolderSessionState.RequestReceived,
            HolderSessionStateStubs.successStub
        ).map {
            HolderSessionState.RequestReceived to it
        }
        private val processingResponseTransitions = listOf(
            HolderSessionState.NotStarted,
            HolderSessionStateStubs.preflightEmptyPermissions,
            HolderSessionState.ReadyToPresent,
            HolderSessionState.PresentingEngagement(""),
            HolderSessionState.Connecting,
            HolderSessionState.RequestReceived
        ).map {
            HolderSessionState.ProcessingResponse to it
        }

        val inputs: List<Pair<HolderSessionState, HolderSessionState>> =
            notStartedTransitions +
                preflightTransitions +
                readyToPresentTransitions +
                presentingEngagementTransitions +
                connectingTransitions +
                requestReceivedTransitions +
                processingResponseTransitions
    }
}
