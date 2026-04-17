package uk.gov.onelogin.sharing.orchestration

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.FakePeripheralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothState
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.PeripheralBluetoothTransportError
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.holder.FakeHolderCryptoService
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoService
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoServiceImpl
import uk.gov.onelogin.sharing.cryptoService.secureArea.session.SessionKeyGenerator.Companion.DeviceRole
import uk.gov.onelogin.sharing.cryptoService.usecases.FakeDecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.CANNOT_TRANSITION_TO_STATE
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.exceptions.BluetoothDisconnectedException
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CompleteHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.HolderSessionContextStub.holderSessionContextStub
import uk.gov.onelogin.sharing.orchestration.holder.session.data.UncancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.hasMissingPreflightPrerequisites
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.inPresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isAwaitingUserConsent
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isFailed
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isProcessingEstablishment
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.session.matchers.FakeSessionFactoryMatchers.currentSessionState
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.UnrecoverableThrowableMatchers.hasSessionErrorThrowable
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnrecoverablePrerequisite
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnrecoverableThrowable

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

    private var prerequisiteResponses: MutableList<MissingPrerequisite> = mutableListOf()

    private val gate by lazy {
        StubPrerequisiteGate(prerequisiteResponses)
    }

    private val fakeDecryptDeviceRequestUseCase = FakeDecryptDeviceRequestUseCase()

    private fun createSessionFactory() = FakeSessionFactory(
        initialStates.map { initialState ->
            HolderSessionImpl(
                logger = logger,
                internalState = MutableStateFlow(initialState),
                initialContext = holderSessionContextStub
            )
        }
    )

    private fun createOrchestrator(
        peripheralBluetoothTransport: PeripheralBluetoothTransport =
            FakePeripheralBluetoothTransport(),
        sessionFactory: SessionFactory<HolderSessionImpl> = createSessionFactory(),
        holderCryptoService: HolderCryptoService = HolderCryptoServiceImpl(
            sessionSecurity = FakeSessionSecurity(),
            logger = logger
        )
    ) = HolderOrchestrator(
        logger = logger,
        sessionFactory = sessionFactory,
        prerequisiteGate = gate,
        peripheralBluetoothTransport = peripheralBluetoothTransport,
        appCoroutineScope = scope,
        decryptDeviceRequestUseCase = fakeDecryptDeviceRequestUseCase,
        credentialProvider = FakeCredentialProvider(),
        holderCryptoService = holderCryptoService
    )

    @Test
    fun `Starting the Orchestrator journey navigates to the PresentingEngagement state`() =
        runTest {
            val sessionFactory = createSessionFactory()
            val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
            backgroundScope.launch {
                orchestrator.holderSessionState.collect {}
            }
            orchestrator.start()

            assert(START_ORCHESTRATION_SUCCESS in logger)
            assert(START_ORCHESTRATION_ERROR !in logger)

            assertThat(
                orchestrator.holderSessionState.value,
                inPresentingEngagement()
            )

            assert(
                logger.any {
                    it.message.startsWith("Performed holder prerequisite checks: ")
                }
            )
        }

    @Test
    fun `Starting without meeting prerequisites then navigates to Preflight state`() = runTest {
        prerequisiteResponses.add(
            MissingPrerequisite.Bluetooth(BluetoothState.PoweredOff)
        )
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.holderSessionState.value,
            hasMissingPreflightPrerequisites(Prerequisite.BLUETOOTH)
        )
    }

    @Test
    fun `Incapable prerequisite check responses transition to failed`() = runTest {
        prerequisiteResponses.add(
            MissingPrerequisite.Bluetooth(BluetoothState.Unsupported)
        )
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.holderSessionState.value,
            isFailed(
                hasReason(isUnrecoverablePrerequisite())
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
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        assert(startSessionAfterCompletionLog in logger)
        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.holderSessionState.value,
            inPresentingEngagement()
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
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.cancel()

        assert(
            CANNOT_TRANSITION_TO_STATE.format(
                state,
                HolderSessionState.Complete.Cancelled
            ) in logger
        )
        assert(
            "$TRANSITION_SUCCESSFUL_TO_STATE ${HolderSessionState.Complete.Cancelled}" !in logger
        )
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
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.cancel()

        assert("$TRANSITION_SUCCESSFUL_TO_STATE ${HolderSessionState.Complete.Cancelled}" in logger)
        assert("$CANNOT_TRANSITION_TO_STATE ${HolderSessionState.Complete.Cancelled}" !in logger)
        assertThat(
            orchestrator.holderSessionState.value,
            isCancelled()
        )
    }

    @Test
    fun `Resetting the Orchestrator clears the HolderSession`() = runTest {
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(sessionFactory = sessionFactory)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            sessionFactory,
            currentSessionState(isNotStarted())
        )
    }

    @Test
    fun `handles device connected state change`() = runTest {
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralBluetoothTransport
        )
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            state = PeripheralBluetoothState.Connected(DEVICE_ADDRESS)
        )

        assert("Mdoc - Connected: $DEVICE_ADDRESS" in logger)
        assertThat(
            orchestrator.holderSessionState.value,
            isProcessingEstablishment()
        )
    }

    @Test
    fun `handles device disconnected state change`() = runTest {
        val sessionFactory = createSessionFactory()
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralBluetoothTransport
        )
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false)
        )

        assert("Error Mdoc - Disconnected: $DEVICE_ADDRESS" in logger)
        assertEquals(1, peripheralBluetoothTransport.stopCalls)

        assertThat(
            orchestrator.holderSessionState.value,
            isFailed(
                hasReason(
                    isUnrecoverableThrowable(
                        hasSessionErrorThrowable(
                            instanceOf(BluetoothDisconnectedException::class.java)
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `handles device disconnected state change when session ended`() = runTest {
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, true)
        )

        assert("BLE session terminated successfully via GATT End command" in logger)
    }

    @Test
    fun `handles error states`() = runTest {
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Error(
                PeripheralBluetoothTransportError.ADVERTISING_FAILED
            )
        )

        assert("Mdoc - Error: Advertising failed" in logger)

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Error(
                PeripheralBluetoothTransportError.GATT_NOT_AVAILABLE
            )
        )

        assert("Mdoc - Error: GATT not available" in logger)

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Error(
                PeripheralBluetoothTransportError.BLUETOOTH_PERMISSION_MISSING
            )
        )

        assert("Mdoc - Error: Bluetooth permission missing" in logger)

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Error(
                PeripheralBluetoothTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED
            )
        )

        assert("Mdoc - Error: Descriptor write request failed" in logger)
    }

    @Test
    fun `logs end session event when session ends`() = runTest {
        val sessionFactory = createSessionFactory()
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralBluetoothTransport,
            sessionFactory = sessionFactory
        )

        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Ended(SessionEndStates.SUCCESS)
        )

        assertThat(
            orchestrator.holderSessionState.value,
            isCancelled()
        )

        assert("Mdoc - Ending session" in logger)
    }

    @Test
    fun `shows error when fails to end session`() = runTest {
        val sessionFactory = createSessionFactory()
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralBluetoothTransport,
            sessionFactory = sessionFactory
        )
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Ended(
                SessionEndStates.NOTIFY_CLIENT_FAILED
            )
        )

        assertThat(
            orchestrator.holderSessionState.value,
            isCancelled()
        )

        assert(
            "Mdoc - Error while ending session: ${SessionEndStates.NOTIFY_CLIENT_FAILED}" in logger
        )
    }

    @Test
    fun `decrypts device request when connected`() = runTest {
        val sessionFactory = createSessionFactory()
        val peripheralTransport = FakePeripheralBluetoothTransport()

        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralTransport
        )
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()
        advanceUntilIdle()

        val currentSession = (sessionFactory as FakeSessionFactory).getCurrentSession()
        assertEquals(1u, currentSession.sessionContext.decryptCounter)

        (orchestrator as HolderOrchestrator).holderSessionState.test {
            assertEquals(
                HolderSessionState.PresentingEngagement(
                    holderSessionContextStub.qrCode
                ),
                awaitItem()
            )

            peripheralTransport.emitState(
                PeripheralBluetoothState.Connected(DEVICE_ADDRESS)
            )

            assertEquals(
                HolderSessionState.ProcessingEstablishment,
                awaitItem()
            )

            peripheralTransport.emitState(
                PeripheralBluetoothState.MessageReceived(
                    byteArrayOf(1, 2, 3)
                )
            )

            assertThat(awaitItem(), isAwaitingUserConsent())
        }

        assertEquals(2u, currentSession.sessionContext.decryptCounter)
    }

    @Test
    fun `CBOR decoding failure builds termination SessionData and transitions to failed`() =
        runTest {
            fakeDecryptDeviceRequestUseCase.exception =
                IllegalArgumentException("CBOR decoding error")
            val peripheralTransport = FakePeripheralBluetoothTransport()
            val sessionFactory = createSessionFactory()
            val orchestrator = createOrchestrator(
                sessionFactory = sessionFactory,
                peripheralBluetoothTransport = peripheralTransport
            )
            backgroundScope.launch {
                orchestrator.holderSessionState.collect {}
            }
            orchestrator.start()
            advanceUntilIdle()

            peripheralTransport.emitState(
                PeripheralBluetoothState.Connected(DEVICE_ADDRESS)
            )
            peripheralTransport.emitState(
                PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
            )
            advanceUntilIdle()

            assertThat(
                orchestrator.holderSessionState.value,
                isFailed()
            )
        }

    @Test
    fun `decryption failure builds termination SessionData and transitions to failed`() = runTest {
        fakeDecryptDeviceRequestUseCase.exception =
            RuntimeException("Decryption failed")
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralTransport
        )
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(
            PeripheralBluetoothState.Connected(DEVICE_ADDRESS)
        )
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
        )
        advanceUntilIdle()

        assertThat(
            orchestrator.holderSessionState.value,
            isFailed()
        )
    }

    @Test
    fun `assembleAndEncryptResponse encrypts DeviceResponse and increments counter`() = runTest {
        val fakeSessionSecurity = FakeSessionSecurity()
        fakeSessionSecurity.encryptedToReturn = byteArrayOf(0x0A, 0x0B, 0x0C)
        val skDevice = byteArrayOf(0x01, 0x02)

        val contextWithSkDevice = holderSessionContextStub.copy(skDevice = skDevice)
        val sessionFactory = FakeSessionFactory(
            listOf(
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(HolderSessionState.NotStarted),
                    initialContext = contextWithSkDevice
                )
            )
        )

        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            holderCryptoService = HolderCryptoServiceImpl(
                sessionSecurity = fakeSessionSecurity,
                logger = logger
            )
        )

        val result = orchestrator.assembleAndEncryptResponse(emptyList())

        val currentSession = sessionFactory.getCurrentSession()
        assertArrayEquals(byteArrayOf(0x0A, 0x0B, 0x0C), result)
        assertArrayEquals(skDevice, fakeSessionSecurity.lastEncryptKey)
        assertEquals(DeviceRole.HOLDER, fakeSessionSecurity.lastEncryptRole)
        assertEquals(1u, fakeSessionSecurity.lastEncryptCounter)
        assertEquals(2u, currentSession.sessionContext.encryptCounter)
    }

    @Test
    fun `parsing failure builds error SessionData with status 11 and transitions to failed`() =
        runTest {
            fakeDecryptDeviceRequestUseCase.exceptionAfterKeyDerivation =
                DeviceRequestDecodingException("CBOR decoding error")
            val fakeCryptoService = FakeHolderCryptoService()
            val peripheralTransport = FakePeripheralBluetoothTransport()
            val sessionFactory = createSessionFactory()
            val orchestrator = createOrchestrator(
                sessionFactory = sessionFactory,
                peripheralBluetoothTransport = peripheralTransport,
                holderCryptoService = fakeCryptoService
            )
            backgroundScope.launch {
                orchestrator.holderSessionState.collect {}
            }
            orchestrator.start()
            advanceUntilIdle()

            peripheralTransport.emitState(
                PeripheralBluetoothState.Connected(DEVICE_ADDRESS)
            )
            peripheralTransport.emitState(
                PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
            )
            advanceUntilIdle()

            assertEquals(
                Status.CBOR_DECODING_ERROR,
                fakeCryptoService.lastErrorDeviceResponseStatus
            )
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                fakeCryptoService.lastErrorSessionDataStatus
            )
            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(0, peripheralTransport.stopCalls)
        }

    @Test
    fun `encryption failure builds termination SessionData and transitions to failed`() = runTest {
        val fakeCryptoService = FakeHolderCryptoService().apply {
            encryptException = RuntimeException("Encryption failed")
        }
        val skDevice = byteArrayOf(0x01, 0x02)
        val contextWithSkDevice = holderSessionContextStub.copy(skDevice = skDevice)
        val sessionFactory = FakeSessionFactory(
            listOf(
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(HolderSessionState.NotStarted),
                    initialContext = contextWithSkDevice
                )
            )
        )
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralTransport,
            holderCryptoService = fakeCryptoService
        )

        assertThrows(RuntimeException::class.java) {
            orchestrator.assembleAndEncryptResponse(emptyList())
        }

        assertEquals(
            SessionDataStatus.SESSION_TERMINATION,
            fakeCryptoService.lastBuildTerminationStatus
        )
        assertEquals(0, peripheralTransport.stopCalls)
    }
}
