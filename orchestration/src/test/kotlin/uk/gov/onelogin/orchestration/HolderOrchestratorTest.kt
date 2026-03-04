package uk.gov.onelogin.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.UUID
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.FakeMdocPeripheralTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralState
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralTransportError
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.MainDispatcherRule
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
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.session.matchers.FakeSessionFactoryMatchers.currentSessionState
import uk.gov.onelogin.sharing.security.usecases.FakeGenerateQrCodeUseCase

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class HolderOrchestratorTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val scope = TestScope(mainDispatcherRule.testDispatcher)
    private val logger = SystemLogger()
    private val resetOrchestratorSessionLog = "Cleared Orchestrator holder session"
    private val startSessionAfterCompletionLog =
        "Starting an Orchestrator holder session after completing the previous journey"

    private var initialStates: MutableList<HolderSessionState> = mutableListOf(
        HolderSessionState.NotStarted,
        HolderSessionState.NotStarted
    )

    private var authorizationResponse = AuthorizationResponse.Authorized

    private val permissionChecker by lazy {
        FakePrerequisiteAuthorizationGate(
            authorizationResponse
        )
    }

    private val fakeGenerateQrEngagement = FakeGenerateQrCodeUseCase()

    private fun createSessionFactory(): SessionFactory<HolderSession> =
        FakeSessionFactory<HolderSession>(
            initialStates.map { initialState ->
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(initialState)
                )
            }
        )

    private fun createOrchestrator(
        mdocPeripheralTransport: MdocPeripheralTransport = FakeMdocPeripheralTransport(),
        sessionFactory: SessionFactory<HolderSession> = createSessionFactory()
    ): Orchestrator = HolderOrchestrator(
        logger = logger,
        sessionFactory = sessionFactory,
        authorizationGate = permissionChecker,
        mdocPeripheralTransport = mdocPeripheralTransport,
        appCoroutineScope = scope,
        engagementData = fakeGenerateQrEngagement
    )

    @Test
    fun `Starting the Orchestrator journey navigates to the Preflight state`() = runTest {
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        orchestrator.start(setOf())

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        /*<<<<<<< HEAD
                assertThat(
                    sessionFactory as FakeSessionFactory,
                    currentSessionState(inPreflight())
                )
        =======*/
        assertThat(
            sessionFactory as FakeSessionFactory,
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
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        orchestrator.start(setOf())

        assert(startSessionAfterCompletionLog in logger)
        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            sessionFactory as FakeSessionFactory,
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
        val sessionFactory = FakeSessionFactory<HolderSession>(
            initialStates.map { _ ->
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(
                        HolderSessionState.Preflight(setOf())
                    )
                )
            }
        )

        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory
        )
        orchestrator.start(setOf())
        advanceUntilIdle()

        assert(START_ORCHESTRATION_ERROR in logger)
        assertThat(
            sessionFactory,
            currentSessionState(inPreflight())
        )
    }

    @Test
    fun `Orchestrator cannot cancel invalid state transitions`(
        @TestParameter(valuesProvider = UncancellableHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialStates[0] = state
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        orchestrator.cancel()

        assert(CANCEL_ORCHESTRATION_ERROR in logger)
        assert(CANCEL_ORCHESTRATION_SUCCESS !in logger)
        assertThat(
            sessionFactory as FakeSessionFactory,
            currentSessionState(state)
        )
    }

    @Test
    fun `Cancelling the User journey is based on the internal session state`(
        @TestParameter(valuesProvider = CancellableHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialStates[0] = state
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        orchestrator.cancel()

        assert(CANCEL_ORCHESTRATION_SUCCESS in logger)
        assert(CANCEL_ORCHESTRATION_ERROR !in logger)
        assertThat(
            sessionFactory as FakeSessionFactory,
            currentSessionState(isCancelled())
        )
    }

    @Test
    fun `Resetting the Orchestrator clears the HolderSession`() = runTest {
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            sessionFactory as FakeSessionFactory,
            currentSessionState(isNotStarted())
        )
    }


    @Test
    fun `handles advertiser started state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        assertEquals(1, mdocPeripheralTransport.startCalls)

        assert(logger.any { it.message.startsWith("Mdoc - Advertising Started") })
    }

    @Test
    fun `handles advertiser stopped state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)
        orchestrator.start(setOf())

        advanceUntilIdle()

        orchestrator.cancel()

        advanceUntilIdle()

        assertEquals(1, mdocPeripheralTransport.stopCalls)

        assert("Mdoc - Advertising Stopped" in logger)
    }

    @Test
    fun `handles device connected state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(MdocPeripheralState.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        assert("Mdoc - Connected: $DEVICE_ADDRESS" in logger)
    }

    @Test
    fun `handles device disconnected state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Disconnected(DEVICE_ADDRESS, false)
        )
        advanceUntilIdle()

        assert("Error Mdoc - Disconnected: $DEVICE_ADDRESS" in logger)
        assertEquals(1, mdocPeripheralTransport.stopCalls)
    }

    @Test
    fun `handles device disconnected state change when session ended`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Disconnected(DEVICE_ADDRESS, true)
        )
        advanceUntilIdle()

        assert("BLE session terminated successfully via GATT End command" in logger)
    }

    @Test
    fun `handles error states`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.ADVERTISING_FAILED
            )
        )
        advanceUntilIdle()
        assert("Mdoc - Error: Advertising failed" in logger)

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.GATT_NOT_AVAILABLE
            )
        )
        advanceUntilIdle()
        assert("Mdoc - Error: GATT not available" in logger)

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.BLUETOOTH_PERMISSION_MISSING
            )
        )
        advanceUntilIdle()
        assert("Mdoc - Error: Bluetooth permission missing" in logger)

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED
            )
        )
        advanceUntilIdle()
        assert("Mdoc - Error: Descriptor write request failed" in logger)
    }

    @Test
    fun `handles gatt service stopped`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.GattServiceStopped
        )
        advanceUntilIdle()

        assert("Mdoc - GattService Stopped" in logger)
    }

    @Test
    fun `handles idle state`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Idle
        )
        advanceUntilIdle()

        assert("Mdoc - Idle" in logger)
    }

    @Test
    fun `handles service added state`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        val uuid = UUID.randomUUID()
        mdocPeripheralTransport.emitState(
            MdocPeripheralState.ServiceAdded(uuid)
        )
        advanceUntilIdle()

        assert("Mdoc - Service Added: $uuid" in logger)
    }

    @Test
    fun `logs end session event when session ends`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.MdocPeripheralEnded(SessionEndStates.SUCCESS)
        )
        advanceUntilIdle()

        assert("Mdoc - Ending session" in logger)
    }

    @Test
    fun `shows error when fails to end session`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start(setOf())
        advanceUntilIdle()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.MdocPeripheralEnded(
                SessionEndStates.NOTIFY_CLIENT_FAILED
            )
        )
        advanceUntilIdle()

        assert(
            "Mdoc - Error while ending session: ${SessionEndStates.NOTIFY_CLIENT_FAILED}" in logger
        )
    }
}
