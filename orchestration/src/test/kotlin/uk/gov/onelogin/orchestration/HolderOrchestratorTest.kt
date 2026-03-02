package uk.gov.onelogin.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.CANCEL_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSession
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CompleteHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.UncancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.inPreflight
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.inPresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.AuthorizationResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.authorization.FakePrerequisiteAuthorizationGate
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.matchers.FakeSessionFactoryMatchers.currentSessionState
import uk.gov.onelogin.sharing.security.usecases.FakeGenerateQrCodeUseCase

@RunWith(TestParameterInjector::class)
class HolderOrchestratorTest {
    private val logger = SystemLogger()
    private val resetOrchestratorSessionLog = "Cleared Orchestrator holder session"
    private val startSessionAfterCompletionLog =
        "Starting an Orchestrator holder session after completing the previous journey"

    private var initialStates: MutableList<HolderSessionState> = mutableListOf(
        HolderSessionState.NotStarted,
        HolderSessionState.NotStarted
    )

    private val sessionFactory by lazy {
        FakeSessionFactory<HolderSession>(
            initialStates.map { initialState ->
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(initialState)
                )
            }
        )
    }

    private var authorizationResponse = AuthorizationResponse.Authorized

    private val permissionChecker by lazy {
        FakePrerequisiteAuthorizationGate(
            authorizationResponse
        )
    }

    private val fakeGenerateQrEngagement = FakeGenerateQrCodeUseCase()

    private val orchestrator by lazy {
        HolderOrchestrator(
            logger = logger,
            sessionFactory = sessionFactory,
            authorizationGate = permissionChecker,
            qrCodeData = fakeGenerateQrEngagement

        )
    }

    @Test
    fun `Starting the Orchestrator journey navigates to the PresentingEngagement state`() =
        runTest {
            orchestrator.start(setOf())

            assert(START_ORCHESTRATION_SUCCESS in logger)
            assert(START_ORCHESTRATION_ERROR !in logger)

            assertThat(
                sessionFactory,
                currentSessionState(inPresentingEngagement())
            )

            assert(
                logger.any { entry ->
                    entry.message.contains(authorizationResponse.toString())
                }
            )
        }

    @Test
    fun `Starting the Orchestrator journey is possible when the journey is already complete`(
        @TestParameter(valuesProvider = CompleteHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialStates[0] = state
        orchestrator.start(setOf())

        assert(startSessionAfterCompletionLog in logger)
        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            sessionFactory,
            currentSessionState(inPresentingEngagement())
        )

        assert(
            logger.any { entry ->
                entry.message.contains(authorizationResponse.toString())
            }
        )
    }

    @Test
    fun `Orchestrator cannot be started when the User journey is already in progress`() = runTest {
        `Starting the Orchestrator journey navigates to the PresentingEngagement state`()

        orchestrator.start(setOf())

        assert(START_ORCHESTRATION_ERROR in logger)
        assertThat(
            sessionFactory,
            currentSessionState(inPresentingEngagement())
        )
    }

    @Test
    fun `Orchestrator cannot cancel invalid state transitions`(
        @TestParameter(valuesProvider = UncancellableHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialStates[0] = state
        orchestrator.cancel()

        assert(CANCEL_ORCHESTRATION_ERROR in logger)
        assert(CANCEL_ORCHESTRATION_SUCCESS !in logger)
        assertThat(
            sessionFactory,
            currentSessionState(state)
        )
    }

    @Test
    fun `Cancelling the User journey is based on the internal session state`(
        @TestParameter(valuesProvider = CancellableHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialStates[0] = state
        orchestrator.cancel()

        assert(CANCEL_ORCHESTRATION_SUCCESS in logger)
        assert(CANCEL_ORCHESTRATION_ERROR !in logger)
        assertThat(
            sessionFactory,
            currentSessionState(isCancelled())
        )
    }

    @Test
    fun `Resetting the Orchestrator clears the HolderSession`() = runTest {
        `Starting the Orchestrator journey navigates to the PresentingEngagement state`()

        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            sessionFactory,
            currentSessionState(isNotStarted())
        )
    }
}
