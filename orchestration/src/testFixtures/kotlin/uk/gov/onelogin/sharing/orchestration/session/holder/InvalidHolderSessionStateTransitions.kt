package uk.gov.onelogin.sharing.orchestration.session.holder

import com.google.testing.junit.testparameterinjector.TestParameters
import com.google.testing.junit.testparameterinjector.TestParametersValuesProvider
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Connecting
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.Initialising
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.NotStarted
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.PresentingEngagement
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ProcessingResponse
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.ReadyToPresent
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionState.RequestReceived
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.preflightEmptyPermissions
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.successStub
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.userCancellation
import uk.gov.onelogin.sharing.orchestration.session.holder.HolderSessionStateStubs.userJourneyFailure

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
            NotStarted,
            preflightEmptyPermissions,
            ReadyToPresent,
            PresentingEngagement,
            Connecting,
            RequestReceived,
            ProcessingResponse,
            successStub,
            userCancellation,
            userJourneyFailure
        ).map {
            NotStarted to it
        }
        private val initialisingTransitions = listOf(
            NotStarted,
            Initialising,
            ReadyToPresent,
            PresentingEngagement,
            Connecting,
            RequestReceived,
            ProcessingResponse,
            successStub,
            userCancellation,
            userJourneyFailure
        ).map {
            Initialising to it
        }
        private val preflightTransitions = listOf(
            NotStarted,
            Initialising,
            preflightEmptyPermissions,
            PresentingEngagement,
            Connecting,
            RequestReceived,
            ProcessingResponse,
            successStub
        ).map {
            preflightEmptyPermissions to it
        }
        private val readyToPresentTransitions = listOf(
            NotStarted,
            Initialising,
            preflightEmptyPermissions,
            ReadyToPresent,
            Connecting,
            RequestReceived,
            ProcessingResponse,
            successStub
        ).map {
            ReadyToPresent to it
        }
        private val presentingEngagementTransitions = listOf(
            NotStarted,
            Initialising,
            preflightEmptyPermissions,
            ReadyToPresent,
            PresentingEngagement,
            RequestReceived,
            ProcessingResponse,
            successStub,
            userJourneyFailure
        ).map {
            PresentingEngagement to it
        }
        private val connectingTransitions = listOf(
            NotStarted,
            Initialising,
            preflightEmptyPermissions,
            ReadyToPresent,
            PresentingEngagement,
            Connecting,
            ProcessingResponse,
            successStub
        ).map {
            Connecting to it
        }
        private val requestReceivedTransitions = listOf(
            NotStarted,
            Initialising,
            preflightEmptyPermissions,
            ReadyToPresent,
            PresentingEngagement,
            Connecting,
            RequestReceived,
            successStub
        ).map {
            RequestReceived to it
        }
        private val processingResponseTransitions = listOf(
            NotStarted,
            Initialising,
            preflightEmptyPermissions,
            ReadyToPresent,
            PresentingEngagement,
            Connecting,
            RequestReceived
        ).map {
            ProcessingResponse to it
        }

        val inputs: List<Pair<HolderSessionState, HolderSessionState>> =
            notStartedTransitions +
                initialisingTransitions +
                preflightTransitions +
                readyToPresentTransitions +
                presentingEngagementTransitions +
                connectingTransitions +
                requestReceivedTransitions +
                processingResponseTransitions
    }
}
