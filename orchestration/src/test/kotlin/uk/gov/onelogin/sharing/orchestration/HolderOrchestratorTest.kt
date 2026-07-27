package uk.gov.onelogin.sharing.orchestration

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
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
import uk.gov.onelogin.sharing.core.sessionTimer.FakeSessionTimer
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequest
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.cryptoService.FakeSessionSecurity
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestDecodingException
import uk.gov.onelogin.sharing.cryptoService.cbor.decoders.DeviceRequestValidationException
import uk.gov.onelogin.sharing.cryptoService.holder.FakeHolderCryptoService
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoService
import uk.gov.onelogin.sharing.cryptoService.holder.HolderCryptoServiceImpl
import uk.gov.onelogin.sharing.cryptoService.usecases.FakeDecryptDeviceRequestUseCase
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.CANNOT_TRANSITION_TO_STATE
import uk.gov.onelogin.sharing.orchestration.Orchestrator.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestException
import uk.gov.onelogin.sharing.orchestration.holder.credential.CredentialRequestHandler
import uk.gov.onelogin.sharing.orchestration.holder.credential.FakeCredentialRequestHandler
import uk.gov.onelogin.sharing.orchestration.holder.credential.NoMatchTerminationCase
import uk.gov.onelogin.sharing.orchestration.holder.credential.ValidatedCredential
import uk.gov.onelogin.sharing.orchestration.holder.session.ConfirmConsentUseCase
import uk.gov.onelogin.sharing.orchestration.holder.session.FakeConfirmConsentUseCase
import uk.gov.onelogin.sharing.orchestration.holder.session.FakeHolderSessionTerminator
import uk.gov.onelogin.sharing.orchestration.holder.session.FakeInboundMessageClassifier
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionImpl
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionTerminator
import uk.gov.onelogin.sharing.orchestration.holder.session.InboundMessageClassifier
import uk.gov.onelogin.sharing.orchestration.holder.session.InboundMessageType
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.CompleteHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.data.HolderSessionContextStub.holderSessionContextStub
import uk.gov.onelogin.sharing.orchestration.holder.session.data.UncancellableHolderSessionStates
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.hasMissingPreflightPrerequisites
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.inPresentingEngagement
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isAwaitingUserConsent
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isAwaitingVerifierResolution
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isFailed
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isProcessingEstablishment
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isProcessingResponse
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isSuccessful
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.SessionFactory
import uk.gov.onelogin.sharing.orchestration.session.matchers.FakeSessionFactoryMatchers.currentSessionState
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isInvalidBluetoothState
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnrecoverablePrerequisite
import uk.gov.onelogin.sharing.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
@Suppress("LargeClass")
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

    private val sessionTimer = FakeSessionTimer()
    private val fakeCredentialRequestHandler = FakeCredentialRequestHandler().apply {
        resultToReturn = ValidatedCredential(
            credentialId = "test-credential-id",
            nameSpaces = byteArrayOf(0xA0.toByte()),
            issuerAuth = byteArrayOf(0x01)
        )
    }

    private fun createSessionFactory() = FakeSessionFactory(
        initialStates.map { initialState ->
            HolderSessionImpl(
                logger = logger,
                internalState = MutableStateFlow(initialState),
                initialContext = holderSessionContextStub
            )
        }
    )

    @SuppressWarnings("LongParameterList")
    private fun createOrchestrator(
        peripheralBluetoothTransport: PeripheralBluetoothTransport =
            FakePeripheralBluetoothTransport(),
        sessionFactory: SessionFactory<HolderSessionImpl> = createSessionFactory(),
        holderCryptoService: HolderCryptoService = HolderCryptoServiceImpl(
            sessionSecurity = FakeSessionSecurity(),
            logger = logger
        ),
        credentialRequestHandler: CredentialRequestHandler = fakeCredentialRequestHandler,
        confirmConsentUseCase: ConfirmConsentUseCase = FakeConfirmConsentUseCase(),
        holderSessionTerminator: HolderSessionTerminator = FakeHolderSessionTerminator(),
        inboundMessageClassifier: InboundMessageClassifier = FakeInboundMessageClassifier()
    ) = HolderOrchestrator(
        logger = logger,
        sessionFactory = sessionFactory,
        prerequisiteGate = gate,
        peripheralBluetoothTransport = peripheralBluetoothTransport,
        appCoroutineScope = scope,
        decryptDeviceRequestUseCase = fakeDecryptDeviceRequestUseCase,
        holderCryptoService = holderCryptoService,
        credentialRequestHandler = credentialRequestHandler,
        confirmConsentUseCase = confirmConsentUseCase,
        holderSessionTerminator = holderSessionTerminator,
        inboundMessageClassifier = inboundMessageClassifier,
        sessionTimer = sessionTimer
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
            MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
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
            MissingPrerequisites.Bluetooth(BluetoothState.Unsupported)
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

        if (!state.isComplete()) {
            assert(
                CANNOT_TRANSITION_TO_STATE.format(
                    state,
                    HolderSessionState.Complete.Cancelled
                ) in logger
            )
        }

        assertThat(
            orchestrator.holderSessionState.value,
            equalTo(state)
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
    fun `ignores BLE state changes when session is already complete`() = runTest {
        initialStates = mutableListOf(
            HolderSessionState.Complete.Success()
        )

        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralBluetoothTransport
        )

        backgroundScope.launch { orchestrator.holderSessionState.collect {} }

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false)
        )

        assert("Session already complete, ignoring BLE state" in logger)
    }

    @Test
    fun `handles device disconnected state change`() = runTest {
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)

        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        orchestrator.holderSessionState.test {
            advanceUntilIdle()
            assertThat(awaitItem(), inPresentingEngagement())
            transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))

            assertThat(awaitItem(), isProcessingEstablishment())
            transport.emitState(PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false))

            assertThat(awaitItem(), isFailed(hasReason(isInvalidBluetoothState())))
        }

        assert("Device $DEVICE_ADDRESS disconnected unexpectedly" in logger)
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
    fun `handles error states`(@TestParameter error: PeripheralBluetoothTransportError) = runTest {
        val peripheralBluetoothTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport)
        backgroundScope.launch {
            orchestrator.holderSessionState.collect {}
        }
        orchestrator.start()

        peripheralBluetoothTransport.emitState(
            PeripheralBluetoothState.Error(error)
        )

        assert("Mdoc - Error: ${error.message}" in logger)
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
        advanceUntilIdle()

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
    fun `transitions to Success when session ends with SUCCESS in AwaitingVerifierResolution`() =
        runTest {
            initialStates[0] = HolderSessionState.AwaitingVerifierResolution
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
            advanceUntilIdle()
            peripheralBluetoothTransport.emitState(
                PeripheralBluetoothState.Ended(SessionEndStates.SUCCESS)
            )
            advanceUntilIdle()
            assertThat(
                orchestrator.holderSessionState.value,
                isSuccessful()
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

        val currentSession = sessionFactory.getCurrentSession()
        assertEquals(1u, currentSession.sessionContext.decryptCounter)

        orchestrator.holderSessionState.test {
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
    fun `confirm consent builds device response and sends over BLE`() = runTest {
        val fakeCryptoService = FakeHolderCryptoService()
        fakeCryptoService.encryptedToReturn = byteArrayOf(0x05, 0x06)
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralTransport,
            holderCryptoService = fakeCryptoService,
            sessionFactory = sessionFactory
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
        )
        advanceUntilIdle()

        orchestrator.confirmConsent()
        advanceUntilIdle()

        assertArrayEquals(
            fakeDecryptDeviceRequestUseCase.skDeviceToReturn,
            fakeCryptoService.lastEncryptSkDevice
        )
        assertEquals(1u, fakeCryptoService.lastEncryptCounter)
        assertEquals(
            2u,
            sessionFactory.getCurrentSession().sessionContext.encryptCounter
        )
        assertThat(orchestrator.holderSessionState.value, isAwaitingVerifierResolution())
    }

    @Test
    fun `confirm consent fails with CannotSendMessage when BLE send fails`() = runTest {
        val fakeCryptoService = FakeHolderCryptoService()
        fakeCryptoService.encryptedToReturn = byteArrayOf(0x05, 0x06)
        val peripheralTransport = FakePeripheralBluetoothTransport()
        peripheralTransport.sendMessageResult = false
        val sessionFactory = createSessionFactory()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralTransport,
            holderCryptoService = fakeCryptoService,
            sessionFactory = sessionFactory
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
        )
        advanceUntilIdle()

        orchestrator.confirmConsent()
        advanceUntilIdle()

        assertThat(
            orchestrator.holderSessionState.value,
            isFailed(
                hasReason(instanceOf(SessionErrorReason.CannotSendMessage::class.java))
            )
        )
    }

    @Test
    fun `sign failure sends status 20 termination and transitions to failed`() = runTest {
        val fakeConfirmConsentUseCase = FakeConfirmConsentUseCase(
            exception = RuntimeException("Signing failed")
        )
        val fakeCryptoService = FakeHolderCryptoService()
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralTransport,
            holderCryptoService = fakeCryptoService,
            confirmConsentUseCase = fakeConfirmConsentUseCase
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(
                byteArrayOf(
                    1,
                    2,
                    3
                )
            )
        )
        advanceUntilIdle()

        orchestrator.confirmConsent()
        advanceUntilIdle()

        assertEquals(
            SessionDataStatus.SESSION_TERMINATION,
            fakeCryptoService.lastBuildTerminationStatus
        )
        assertThat(orchestrator.holderSessionState.value, isFailed())
    }

    @Test
    fun `successful request credential and docType match stores credential`() = runTest {
        val sessionFactory = createSessionFactory()
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory,
            peripheralBluetoothTransport = peripheralTransport
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
        )
        advanceUntilIdle()

        assertThat(orchestrator.holderSessionState.value, isAwaitingUserConsent())
        assert("provided credential matches DeviceRequest docType" in logger)

        val session = sessionFactory.getCurrentSession()
        val validatedCredential = session.sessionContext.validatedCredential
        assertEquals("test-credential-id", validatedCredential?.credentialId)
    }

    @Test
    fun `credential request failure triggers no match termination`(
        @TestParameter case: NoMatchTerminationCase
    ) = runTest {
        val handler = FakeCredentialRequestHandler().apply {
            exceptionToThrow = CredentialRequestException(case.errorMessage)
        }
        val fakeCryptoService = FakeHolderCryptoService()
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val terminator = FakeHolderSessionTerminator()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralTransport,
            holderCryptoService = fakeCryptoService,
            credentialRequestHandler = handler,
            holderSessionTerminator = terminator
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
        )
        advanceUntilIdle()

        assertThat(orchestrator.holderSessionState.value, isSuccessful())
        assertEquals(
            HolderSessionState.Complete.SuccessReason.UnfulfillableRequest,
            (orchestrator.holderSessionState.value as HolderSessionState.Complete.Success)
                .successReason
        )
        assert(case.errorMessage in logger)
        assertEquals(Status.OK, fakeCryptoService.lastErrorDeviceResponseStatus)
        assertEquals(
            SessionDataStatus.SESSION_TERMINATION,
            fakeCryptoService.lastErrorSessionDataStatus
        )
        assert(
            "$TRANSITION_SUCCESSFUL_TO_STATE ${HolderSessionState.SendingTermination}" in logger
        )
        assertEquals(1, terminator.terminateCalls)
        assertEquals(0, peripheralTransport.stopCalls)
    }

    @Test
    fun `filter failure triggers no match termination before consent`() = runTest {
        val fakeCryptoService = FakeHolderCryptoService()
        val peripheralTransport = FakePeripheralBluetoothTransport()
        val terminator = FakeHolderSessionTerminator()
        val failingHandler = FakeCredentialRequestHandler().apply {
            exceptionToThrow = CredentialRequestException("no matching attributes")
        }
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = peripheralTransport,
            holderCryptoService = fakeCryptoService,
            credentialRequestHandler = failingHandler,
            holderSessionTerminator = terminator
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        advanceUntilIdle()

        peripheralTransport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        peripheralTransport.emitState(
            PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3))
        )
        advanceUntilIdle()

        assertThat(orchestrator.holderSessionState.value, isSuccessful())
        assertEquals(
            HolderSessionState.Complete.SuccessReason.UnfulfillableRequest,
            (orchestrator.holderSessionState.value as HolderSessionState.Complete.Success)
                .successReason
        )
        assert("no matching attributes" in logger)
        assertEquals(Status.OK, fakeCryptoService.lastErrorDeviceResponseStatus)
        assertEquals(
            SessionDataStatus.SESSION_TERMINATION,
            fakeCryptoService.lastErrorSessionDataStatus
        )
        assertEquals(1, terminator.terminateCalls)
        assertEquals(0, peripheralTransport.stopCalls)
    }

    @Test
    fun `deny consent sends termination and transitions to Success`() = runTest {
        with(TerminationTestFixture()) {
            startAndDeliver()
            assertThat(orchestrator.holderSessionState.value, isAwaitingUserConsent())

            orchestrator.denyConsent()
            advanceUntilIdle()

            assertThat(orchestrator.holderSessionState.value, isSuccessful())
            assertEquals(Status.OK, cryptoService.lastErrorDeviceResponseStatus)
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastErrorSessionDataStatus
            )
            assertEquals(1, terminator.terminateCalls)
            assert(
                "$TRANSITION_SUCCESSFUL_TO_STATE ${HolderSessionState.SendingTermination}" in logger
            )
        }
    }

    @Test
    fun `deny consent without skDevice transitions to failed`() = runTest {
        val initialState = HolderSessionState.AwaitingUserConsent(deviceRequestStub)
        initialStates = mutableListOf(initialState, HolderSessionState.NotStarted)
        val sessionFactory = FakeSessionFactory(
            listOf(
                HolderSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(initialState),
                    initialContext = holderSessionContextStub.copy(skDevice = null)
                )
            )
        )
        val orchestrator = createOrchestrator(
            sessionFactory = sessionFactory
        )
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }

        orchestrator.denyConsent()
        advanceUntilIdle()

        assertThat(orchestrator.holderSessionState.value, isFailed())
    }

    @Test
    fun `sequencing violation in processingEstablishment sends status 20 and terminates`() =
        runTest {
            with(TerminationTestFixture()) {
                startAndDeliver()
                assertThat(orchestrator.holderSessionState.value, isAwaitingUserConsent())

                // Second message is a sequencing violation (now in AwaitingUserConsent)
                transport.emitState(
                    PeripheralBluetoothState.MessageReceived(byteArrayOf(4, 5, 6))
                )
                advanceUntilIdle()

                assertThat(orchestrator.holderSessionState.value, isFailed())
                assertEquals(
                    SessionDataStatus.SESSION_TERMINATION,
                    cryptoService.lastBuildTerminationStatus
                )
                assertEquals(1, terminator.terminateCalls)
                assert(
                    logger.any { it.message.startsWith("Sequencing violation") }
                )
            }
        }

    @Test
    fun `unrecognised message in processingEstablishment sends status 20 and terminates`() =
        runTest {
            val fakeClassifier = FakeInboundMessageClassifier().apply {
                typeToReturn = InboundMessageType.Unknown
            }
            with(TerminationTestFixture(inboundMessageClassifier = fakeClassifier)) {
                startAndDeliver()

                assertThat(orchestrator.holderSessionState.value, isFailed())
                assertEquals(1, terminator.terminateCalls)
                assertEquals(
                    SessionDataStatus.SESSION_TERMINATION,
                    cryptoService.lastBuildTerminationStatus
                )
            }
        }

    @Test
    fun `sequencing violation in awaitingUserConsent sends status 20 and terminates`() = runTest {
        with(TerminationTestFixture()) {
            startAndDeliver()
            assertThat(orchestrator.holderSessionState.value, isAwaitingUserConsent())

            transport.emitState(
                PeripheralBluetoothState.MessageReceived(byteArrayOf(4, 5, 6))
            )
            advanceUntilIdle()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(1, terminator.terminateCalls)
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastBuildTerminationStatus
            )
        }
    }

    @Test
    fun `sequencing violation in processingResponse sends status 20 and terminates`() = runTest {
        val consentGate = kotlinx.coroutines.CompletableDeferred<Unit>()
        with(
            TerminationTestFixture(
                confirmConsentUseCase = FakeConfirmConsentUseCase(gate = consentGate)
            )
        ) {
            startAndDeliver()
            assertThat(orchestrator.holderSessionState.value, isAwaitingUserConsent())

            orchestrator.confirmConsent()
            advanceUntilIdle()
            assertThat(orchestrator.holderSessionState.value, isProcessingResponse())

            // Message arrives while in ProcessingResponse — sequencing violation
            transport.emitState(
                PeripheralBluetoothState.MessageReceived(byteArrayOf(4, 5, 6))
            )
            advanceUntilIdle()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(1, terminator.terminateCalls)
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastBuildTerminationStatus
            )
        }
    }

    @Test
    fun `SessionEstablishment decryption failure sends status 20 and terminates`() = runTest {
        fakeDecryptDeviceRequestUseCase.exception =
            RuntimeException("Decryption failed")
        with(TerminationTestFixture()) {
            startAndDeliver()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastBuildTerminationStatus
            )
            assertEquals(1, terminator.terminateCalls)
        }
    }

    @Test
    fun `DeviceRequest decode failure sends status 11 over BLE and terminates`() = runTest {
        fakeDecryptDeviceRequestUseCase.exceptionAfterKeyDerivation =
            DeviceRequestDecodingException("CBOR decoding error")
        with(TerminationTestFixture()) {
            startAndDeliver()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(Status.CBOR_DECODING_ERROR, cryptoService.lastErrorDeviceResponseStatus)
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastErrorSessionDataStatus
            )
            assertEquals(1, terminator.terminateCalls)
        }
    }

    @Test
    fun `DeviceRequest validation failure sends status 12 over BLE and terminates`() = runTest {
        fakeDecryptDeviceRequestUseCase.exceptionAfterKeyDerivation =
            DeviceRequestValidationException("empty DocRequest")
        with(TerminationTestFixture()) {
            startAndDeliver()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(
                Status.CBOR_VALIDATION_ERROR,
                cryptoService.lastErrorDeviceResponseStatus
            )
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastErrorSessionDataStatus
            )
            assertEquals(1, terminator.terminateCalls)
        }
    }

    @Test
    fun `missing portrait attribute sends status 10 over BLE and terminates`() = runTest {
        // Return a DeviceRequest without portrait
        fakeDecryptDeviceRequestUseCase.deviceRequestToReturn = deviceRequest(
            mapOf("age_over_18" to false)
        )
        with(TerminationTestFixture()) {
            startAndDeliver()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(Status.GENERAL_ERROR, cryptoService.lastErrorDeviceResponseStatus)
            assertEquals(
                SessionDataStatus.SESSION_TERMINATION,
                cryptoService.lastErrorSessionDataStatus
            )
            assertEquals(1, terminator.terminateCalls)
            assert("Policy violation: DeviceRequest does not request portrait attribute" in logger)
        }
    }

    @Test
    fun `sendTerminationAndFail skips terminate when BLE send fails`() = runTest {
        fakeDecryptDeviceRequestUseCase.exception =
            RuntimeException("Decryption failed")
        with(TerminationTestFixture()) {
            transport.sendMessageResult = false
            startAndDeliver()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assertEquals(0, terminator.terminateCalls)
        }
    }

    @Test
    fun `status-only SessionData in processingEstablishment is peer termination`() = runTest {
        val fakeClassifier = FakeInboundMessageClassifier().apply {
            typeToReturn =
                InboundMessageType.StatusOnly(SessionDataStatus.SESSION_TERMINATION)
        }
        with(TerminationTestFixture(inboundMessageClassifier = fakeClassifier)) {
            startAndDeliver()

            // Should be treated as peer termination (failed), not sequencing violation
            assertThat(orchestrator.holderSessionState.value, isFailed())
            // No outbound termination should be sent for a peer-initiated termination
            assertEquals(0, terminator.terminateCalls)
            assert(
                logger.any { it.message.contains("Peer termination received") }
            )
        }
    }

    @Test
    fun `status-only SessionData in awaitingUserConsent is peer termination`() = runTest {
        val fakeClassifier = FakeInboundMessageClassifier()
        with(TerminationTestFixture(inboundMessageClassifier = fakeClassifier)) {
            startAndDeliver()
            assertThat(orchestrator.holderSessionState.value, isAwaitingUserConsent())

            fakeClassifier.typeToReturn =
                InboundMessageType.StatusOnly(SessionDataStatus.SESSION_TERMINATION)

            transport.emitState(
                PeripheralBluetoothState.MessageReceived(byteArrayOf(4, 5, 6))
            )
            advanceUntilIdle()

            assertThat(orchestrator.holderSessionState.value, isFailed())
            assert(
                logger.any { it.message.contains("Peer termination received") }
            )
        }
    }

    @Test
    fun `status 20 SessionData in awaitingVerifierResolution transitions to success`() = runTest {
        with(PeerTerminationFixture(SessionDataStatus.SESSION_TERMINATION)) {
            deliverMessage()

            assertThat(orchestrator.holderSessionState.value, isSuccessful())
        }
    }

    @Test
    fun `non-20 status SessionData in awaitingVerifierResolution transitions to failed`() =
        runTest {
            with(PeerTerminationFixture(SessionDataStatus.ERROR_SESSION_ENCRYPTION)) {
                deliverMessage()

                assertThat(orchestrator.holderSessionState.value, isFailed())
                val failedState =
                    orchestrator.holderSessionState.value as HolderSessionState.Complete.Failed
                assertThat(
                    failedState.sessionReason,
                    instanceOf(SessionErrorReason.StatusError::class.java)
                )
                assertEquals(
                    SessionDataStatus.ERROR_SESSION_ENCRYPTION.code,
                    (failedState.sessionReason as SessionErrorReason.StatusError).statusCode
                )
            }
        }

    @Test
    fun `SessionData then GATT End in awaitingVerifierResolution ignores second signal`() =
        runTest {
            with(PeerTerminationFixture(SessionDataStatus.SESSION_TERMINATION)) {
                deliverMessage()
                assertThat(orchestrator.holderSessionState.value, isSuccessful())

                // Second signal: GATT End — should be ignored
                transport.emitState(
                    PeripheralBluetoothState.Ended(SessionEndStates.SUCCESS)
                )
                advanceUntilIdle()

                assertThat(orchestrator.holderSessionState.value, isSuccessful())
            }
        }

    @Test
    fun `no outbound signal sent on peer termination in awaitingVerifierResolution`() = runTest {
        with(PeerTerminationFixture(SessionDataStatus.SESSION_TERMINATION)) {
            deliverMessage()

            assertThat(orchestrator.holderSessionState.value, isSuccessful())
            assertEquals(0, terminator.terminateCalls)
            assertEquals(0, transport.sendMessageCalls)
            assertEquals(false, transport.lastStopSendEndCommand)
        }
    }

    @Test
    fun `connection loss during active processing results in failure`(
        @TestParameter state: HolderSessionState = namedTestValues(
            "Processing establishment" to HolderSessionState.ProcessingEstablishment,
            "Awaiting user consent" to HolderSessionState.AwaitingUserConsent(deviceRequestStub),
            "Processing response" to HolderSessionState.ProcessingResponse
        )
    ) = runTest {
        initialStates[0] = state
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)

        backgroundScope.launch { orchestrator.holderSessionState.collect {} }

        transport.emitState(PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false))
        advanceUntilIdle()

        orchestrator.holderSessionState.test {
            awaitItem()
            assertThat("Failed for state $state", orchestrator.holderSessionState.value, isFailed())
        }
    }

    @Test
    fun `connection loss in terminal state is ignored`(
        @TestParameter(valuesProvider = CompleteHolderSessionStates::class)
        state: HolderSessionState
    ) = runTest {
        initialStates[0] = state
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)

        backgroundScope.launch { orchestrator.holderSessionState.collect {} }

        transport.emitState(PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false))
        advanceUntilIdle()

        assertEquals("State changed from $state", state, orchestrator.holderSessionState.value)
    }

    @Test
    fun `connection loss in AwaitingVerifierResolution results in success`() = runTest {
        initialStates[0] = HolderSessionState.AwaitingVerifierResolution
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)

        backgroundScope.launch { orchestrator.holderSessionState.collect {} }

        transport.emitState(PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false))
        advanceUntilIdle()

        assertThat(orchestrator.holderSessionState.value, isSuccessful())
    }

    @Test
    fun `connection loss during terminatingSession is suppressed`() = runTest {
        initialStates[0] = HolderSessionState.SendingTermination
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)

        backgroundScope.launch { orchestrator.holderSessionState.collect {} }

        transport.emitState(PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false))
        advanceUntilIdle()

        assertThat(
            orchestrator.holderSessionState.value,
            equalTo(HolderSessionState.SendingTermination)
        )
        assert("Session complete or terminating, ignoring BLE state" in logger)
    }

    @Test
    fun `connection loss during outbound message sending is suppressed`() = runTest {
        val initialState = HolderSessionState.AwaitingUserConsent(deviceRequestStub)
        initialStates[0] = initialState

        val sessionWithContext = HolderSessionImpl(
            logger = logger,
            internalState = MutableStateFlow(initialState),
            initialContext = holderSessionContextStub.copy(
                skDevice = byteArrayOf(0x01, 0x02)
            )
        )
        val sessionFactory = FakeSessionFactory(listOf(sessionWithContext))

        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = transport,
            sessionFactory = sessionFactory
        )

        val messageSentDeferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
        transport.sendMessageResultDeferred = messageSentDeferred

        orchestrator.holderSessionState.test {
            assertEquals(initialState, awaitItem())

            orchestrator.denyConsent()

            var currentState = awaitItem()
            if (currentState is HolderSessionState.ProcessingResponse) {
                currentState = awaitItem()
            }

            assertEquals(HolderSessionState.SendingTermination, currentState)

            transport.emitState(PeripheralBluetoothState.Disconnected(DEVICE_ADDRESS, false))
            messageSentDeferred.complete(true)

            assertThat(awaitItem(), isSuccessful())

            expectNoEvents()
        }

        assert("Session complete or terminating, ignoring BLE state" in logger)
    }

    @Test
    fun `timer starts with 300 seconds when device connects`() = runTest {
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()

        transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        assertEquals(1, sessionTimer.startCalls)
        kotlin.test.assertEquals(
            300.seconds,
            sessionTimer.lastDuration
        )
    }

    @Test
    fun `cancels when timer fires`() = runTest {
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        sessionTimer.onTimeout?.invoke()
        advanceUntilIdle()

        assertThat(orchestrator.holderSessionState.value, isCancelled())
        assertEquals(1, transport.stopCalls)
    }

    @Test
    fun `timer resets when an inbound message is received`() = runTest {
        val transport = FakePeripheralBluetoothTransport()
        val orchestrator = createOrchestrator(peripheralBluetoothTransport = transport)
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()
        transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        transport.emitState(PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3)))
        advanceUntilIdle()

        assertEquals(1, sessionTimer.resetCalls)
    }

    @Test
    fun `timer resets when DeviceResponse is sent after confirming consent`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val transport = FakePeripheralBluetoothTransport()
            val orchestrator = createOrchestrator(
                peripheralBluetoothTransport = transport,
                holderCryptoService = FakeHolderCryptoService()
            )

            orchestrator.holderSessionState.test {
                awaitItem()

                orchestrator.start()
                skipItems(2)

                transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
                awaitItem()

                transport.emitState(PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3)))
                awaitItem()

                val resetCallsBeforeOutbound = sessionTimer.resetCalls

                orchestrator.confirmConsent()

                awaitItem()
                awaitItem()

                assertEquals(
                    resetCallsBeforeOutbound + 1,
                    sessionTimer.resetCalls
                )
            }
        }

    @Test
    fun `timer resets when termination message is sent after denying consent`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val transport = FakePeripheralBluetoothTransport()
            val orchestrator = createOrchestrator(
                peripheralBluetoothTransport = transport,
                holderCryptoService = FakeHolderCryptoService()
            )

            orchestrator.holderSessionState.test {
                awaitItem()

                orchestrator.start()
                skipItems(2)

                transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
                awaitItem()

                transport.emitState(PeripheralBluetoothState.MessageReceived(byteArrayOf(1, 2, 3)))
                awaitItem()

                val resetCallsBeforeOutbound = sessionTimer.resetCalls

                orchestrator.denyConsent()

                awaitItem()
                awaitItem()

                assertEquals(
                    resetCallsBeforeOutbound + 1,
                    sessionTimer.resetCalls
                )
            }
        }

    @Test
    fun `Timer stops when the session is cancelled`() = runTest {
        val orchestrator = createOrchestrator()
        backgroundScope.launch { orchestrator.holderSessionState.collect {} }
        orchestrator.start()

        orchestrator.cancel()
        advanceUntilIdle()

        assertEquals(1, sessionTimer.stopCalls)
    }

    private inner class PeerTerminationFixture(
        status: SessionDataStatus,
        initialState: HolderSessionState = HolderSessionState.AwaitingVerifierResolution
    ) {
        val transport = FakePeripheralBluetoothTransport()
        val terminator = FakeHolderSessionTerminator()
        val orchestrator: HolderOrchestrator

        init {
            initialStates[0] = initialState
            val fakeClassifier = FakeInboundMessageClassifier().apply {
                typeToReturn = InboundMessageType.StatusOnly(status)
            }
            orchestrator = createOrchestrator(
                peripheralBluetoothTransport = transport,
                sessionFactory = createSessionFactory(),
                inboundMessageClassifier = fakeClassifier,
                holderSessionTerminator = terminator
            )
        }

        fun TestScope.deliverMessage(bytes: ByteArray = byteArrayOf(1, 2, 3)) {
            backgroundScope.launch { orchestrator.holderSessionState.collect {} }
            orchestrator.start()
            transport.emitState(PeripheralBluetoothState.MessageReceived(bytes))
            advanceUntilIdle()
        }
    }

    private inner class TerminationTestFixture(
        val cryptoService: FakeHolderCryptoService = FakeHolderCryptoService(),
        val transport: FakePeripheralBluetoothTransport = FakePeripheralBluetoothTransport(),
        val terminator: FakeHolderSessionTerminator = FakeHolderSessionTerminator(),
        confirmConsentUseCase: ConfirmConsentUseCase = FakeConfirmConsentUseCase(),
        inboundMessageClassifier: InboundMessageClassifier = FakeInboundMessageClassifier()
    ) {
        val orchestrator = createOrchestrator(
            peripheralBluetoothTransport = transport,
            holderCryptoService = cryptoService,
            holderSessionTerminator = terminator,
            confirmConsentUseCase = confirmConsentUseCase,
            inboundMessageClassifier = inboundMessageClassifier
        )

        fun TestScope.startAndDeliver(bytes: ByteArray = byteArrayOf(1, 2, 3)) {
            backgroundScope.launch { orchestrator.holderSessionState.collect {} }
            orchestrator.start()
            advanceUntilIdle()
            transport.emitState(PeripheralBluetoothState.Connected(DEVICE_ADDRESS))
            transport.emitState(PeripheralBluetoothState.MessageReceived(bytes))
            advanceUntilIdle()
        }
    }
}
