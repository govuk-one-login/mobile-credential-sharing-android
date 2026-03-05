package uk.gov.onelogin.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Ignore
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
import uk.gov.onelogin.sharing.orchestration.holder.session.data.HolderSessionContextStub.dummyHolderSessionContext
import uk.gov.onelogin.sharing.orchestration.holder.session.data.UncancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isReadyToPresent
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isRequestReceived
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.hasMissingPreflightPrerequisites
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.session.matchers.FakeSessionFactoryMatchers.currentSessionState
import uk.gov.onelogin.sharing.security.usecases.FakeDecryptDeviceRequestUseCase

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

    private var prerequisiteResponse: MutableMap<Prerequisite, PrerequisiteResponse> =
        Prerequisite.entries.associateWith {
            PrerequisiteResponse.MeetsPrerequisites
        }.toMutableMap()

    private val gate by lazy {
        StubPrerequisiteGate(prerequisiteResponse)
    }

    private val fakeDecryptDeviceRequestUseCase = FakeDecryptDeviceRequestUseCase()

    private fun createSessionFactory(): SessionFactory<HolderSession> =
        FakeSessionFactory<HolderSession>(
            initialStates.map { initialState ->
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(initialState),
                    sessionContext = dummyHolderSessionContext
                )
            }
        )

    private fun createOrchestrator(
        mdocPeripheralTransport: MdocPeripheralTransport = FakeMdocPeripheralTransport(),
        sessionFactory: SessionFactory<HolderSession> = createSessionFactory()
    ): Orchestrator = HolderOrchestrator(
        logger = logger,
        sessionFactory = sessionFactory,
        prerequisiteGate = gate,
        mdocPeripheralTransport = mdocPeripheralTransport,
        appCoroutineScope = scope,
        decryptDeviceRequestUseCase = fakeDecryptDeviceRequestUseCase
    )

    @Test
    fun `Starting the Orchestrator journey navigates to the PresentingEngagement state`() =
        runTest {
            val sessionFactory = createSessionFactory()
            val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
            orchestrator.start()

            assert(START_ORCHESTRATION_SUCCESS in logger)
            assert(START_ORCHESTRATION_ERROR !in logger)

            assertThat(
                sessionFactory as FakeSessionFactory,
                currentSessionState(isReadyToPresent())
            )

            assert(
                logger.any {
                    it.message.startsWith("Performed holder prerequisite checks: ")
                }
            )
        }

    @Test
    fun `Starting without meeting prerequisites then navigates to Preflight state`() = runTest {
        prerequisiteResponse[Prerequisite.BLUETOOTH] = PrerequisiteResponse.Incapable(
            IncapableReason.MissingHardware
        )
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            sessionFactory as FakeSessionFactory,
            currentSessionState(
                hasMissingPreflightPrerequisites(Prerequisite.BLUETOOTH)
            )
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
        orchestrator.start()

        assert(startSessionAfterCompletionLog in logger)
        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            sessionFactory as FakeSessionFactory,
            currentSessionState(isReadyToPresent())
        )
    }

    @Test
    fun `Orchestrator cannot be started when the User journey is already in progress`() = runTest {
        val orchestrator = createOrchestrator()

        orchestrator.start()

        orchestrator.start()

        assert(START_ORCHESTRATION_ERROR in logger)
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

        orchestrator.start()

        assertEquals(1, mdocPeripheralTransport.startCalls)

        assert(logger.any { it.message.startsWith("Mdoc - Advertising Started") })
    }

    @Test
    fun `handles advertiser stopped state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)
        orchestrator.start()

        orchestrator.cancel()

        assertEquals(1, mdocPeripheralTransport.stopCalls)

        assert("Mdoc - Advertising Stopped" in logger)
    }

    @Test
    fun `handles device connected state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(MdocPeripheralState.Connected(DEVICE_ADDRESS))

        assert("Mdoc - Connected: $DEVICE_ADDRESS" in logger)
    }

    @Test
    fun `handles device disconnected state change`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Disconnected(DEVICE_ADDRESS, false)
        )

        assert("Error Mdoc - Disconnected: $DEVICE_ADDRESS" in logger)
        assertEquals(1, mdocPeripheralTransport.stopCalls)
    }

    @Test
    fun `handles device disconnected state change when session ended`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Disconnected(DEVICE_ADDRESS, true)
        )

        assert("BLE session terminated successfully via GATT End command" in logger)
    }

    @Test
    fun `handles error states`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.ADVERTISING_FAILED
            )
        )

        assert("Mdoc - Error: Advertising failed" in logger)

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.GATT_NOT_AVAILABLE
            )
        )

        assert("Mdoc - Error: GATT not available" in logger)

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.BLUETOOTH_PERMISSION_MISSING
            )
        )

        assert("Mdoc - Error: Bluetooth permission missing" in logger)

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED
            )
        )

        assert("Mdoc - Error: Descriptor write request failed" in logger)
    }

    @Test
    fun `handles gatt service stopped`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.GattServiceStopped
        )

        assert("Mdoc - GattService Stopped" in logger)
    }

    @Test
    fun `handles idle state`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.Idle
        )

        assert("Mdoc - Idle" in logger)
    }

    @Test
    fun `handles service added state`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        val uuid = UUID.randomUUID()
        mdocPeripheralTransport.emitState(
            MdocPeripheralState.ServiceAdded(uuid)
        )

        assert("Mdoc - Service Added: $uuid" in logger)
    }

    @Test
    fun `logs end session event when session ends`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.MdocPeripheralEnded(SessionEndStates.SUCCESS)
        )

        assert("Mdoc - Ending session" in logger)
    }

    @Test
    fun `shows error when fails to end session`() = runTest {
        val mdocPeripheralTransport = FakeMdocPeripheralTransport()
        val orchestrator = createOrchestrator(mdocPeripheralTransport)

        orchestrator.start()

        mdocPeripheralTransport.emitState(
            MdocPeripheralState.MdocPeripheralEnded(
                SessionEndStates.NOTIFY_CLIENT_FAILED
            )
        )

        assert(
            "Mdoc - Error while ending session: ${SessionEndStates.NOTIFY_CLIENT_FAILED}" in logger
        )
    }

    @Ignore
    @Test
    fun `decrypts device request when session ends`() = runTest {
        val sessionFactory = createSessionFactory()
        val peripheralTransport =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.AdvertisingStarted
            )
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            mdocPeripheralTransport = peripheralTransport
        )
        orchestrator.start()

        peripheralTransport.emitState(
            MdocPeripheralState.MessageReceived(
                byteArrayOf(1, 2, 3)
            )
        )

        assertThat(
            sessionFactory as FakeSessionFactory,
            currentSessionState(isRequestReceived())
        )
    }
}
