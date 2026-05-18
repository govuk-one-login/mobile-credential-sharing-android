package uk.gov.onelogin.sharing.orchestration

import app.cash.turbine.test
import com.fasterxml.jackson.databind.JsonMappingException
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertArrayEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothState
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransportError
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.FakeCentralBluetoothTransport
import uk.gov.onelogin.sharing.bluetooth.internal.central.GattUuids.SERVER_2_CLIENT_UUID
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_MDOC_URI
import uk.gov.onelogin.sharing.cryptoService.cbor.CborMapper
import uk.gov.onelogin.sharing.cryptoService.cbor.dto.SessionDataDto.Companion.toDto
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.cryptoService.verifier.DeferredVerifierCryptoService
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.cryptoService.verifier.FakeVerifierCryptoService
import uk.gov.onelogin.sharing.cryptoService.verifier.SessionEstablishmentException
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnrecoverablePrerequisite
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.nameRetainAndAgeOver18Config
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.photoAndAgeOver21Config
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionImpl
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CompleteVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.UncancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.hasMissingPreflightPrerequisites
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isConnecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isFailed
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isReadyToScan

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class VerifierOrchestratorTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = SystemLogger()
    private val resetOrchestratorSessionLog = "Cleared Orchestrator verifier session"
    private val startSessionAfterCompletionLog =
        "Starting an Orchestrator verifier session after completing the previous journey"

    private var initialStates: MutableList<VerifierSessionState> = mutableListOf(
        VerifierSessionState.NotStarted,
        VerifierSessionState.NotStarted
    )

    private val sessionFactory by lazy {
        FakeSessionFactory(
            initialStates.map { initialState ->
                VerifierSessionImpl(
                    logger = logger,
                    internalState = MutableStateFlow(initialState)
                )
            }
        )
    }

    private var gateResponses: MutableList<MissingPrerequisite> = mutableListOf()

    private val gate by lazy {
        StubPrerequisiteGate(gateResponses)
    }

    private val centralBluetoothTransport = FakeCentralBluetoothTransport()
    private val fakeCryptoService = FakeVerifierCryptoService()
    private var verifierCryptoService: VerifierCryptoService = fakeCryptoService

    private val scope = TestScope(mainDispatcherRule.testDispatcher)

    private val orchestrator by lazy {
        VerifierOrchestrator(
            logger = logger,
            prerequisiteGate = gate,
            sessionFactory = sessionFactory,
            verifierConfig = verifierConfigStub,
            centralBluetoothTransport = centralBluetoothTransport,
            appCoroutineScope = scope,
            barcodeParser = FakeQrParser(),
            verifierCryptoService = verifierCryptoService
        )
    }

    @Before
    fun setUp() {
        assertEquals(
            0,
            logger.size
        )
    }

    @Test
    fun `Starting the Orchestrator journey navigates to the Preflight state`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.verifierSessionState.value,
            isReadyToScan()
        )
    }

    @Test
    fun `Starting without meeting prerequisites then navigates to Preflight state`() = runTest {
        gateResponses.add(
            MissingPrerequisite.Bluetooth(BluetoothState.PoweredOff)
        )

        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.verifierSessionState.value,
            hasMissingPreflightPrerequisites(Prerequisite.BLUETOOTH)
        )
    }

    @Test
    fun `Incapable prerequisite check responses transition to failed`() = runTest {
        gateResponses.add(
            MissingPrerequisite.Bluetooth(BluetoothState.Unsupported)
        )

        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed(
                hasReason(isUnrecoverablePrerequisite())
            )
        )
    }

    @Test
    fun `Starting the Orchestrator journey is possible when the journey is already complete`(
        @TestParameter(valuesProvider = CompleteVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        initialStates[0] = state
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        assert(startSessionAfterCompletionLog in logger)
        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.verifierSessionState.value,
            isReadyToScan()
        )
    }

    @Test
    fun `Orchestrator cannot be started more than once`() = runTest {
        `Starting the Orchestrator journey navigates to the Preflight state`()
        orchestrator.start()

        assert(START_ORCHESTRATION_ERROR in logger)
        assertThat(
            orchestrator.verifierSessionState.value,
            isReadyToScan()
        )
    }

    @Test
    fun `Orchestrator cannot cancel invalid state transitions`(
        @TestParameter(valuesProvider = UncancellableVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        initialStates[0] = state
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.cancel()

        assertThat(
            orchestrator.verifierSessionState.value,
            equalTo(state)
        )
    }

    @Test
    fun `Cancelling the User journey is based on the internal session state`(
        @TestParameter(valuesProvider = CancellableVerifierSessionStates::class)
        state: VerifierSessionState
    ) = runTest {
        initialStates[0] = state
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.cancel()

        assertThat(
            orchestrator.verifierSessionState.value,
            isCancelled()
        )
    }

    @Test
    fun `Resetting the Orchestrator clears the VerifierSession`() = runTest {
        `Starting the Orchestrator journey navigates to the Preflight state`()

        orchestrator.reset()

        assert(resetOrchestratorSessionLog in logger)
        assertThat(
            orchestrator.verifierSessionState.value,
            isNotStarted()
        )
    }

    @Test
    fun `processQrCode with valid barcode transitions to Connecting`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }

        val data = VALID_MDOC_URI

        orchestrator.start()

        orchestrator.processQrCode(data)

        assertThat(
            orchestrator.verifierSessionState.value,
            isConnecting()
        )

        val currentState =
            orchestrator.verifierSessionState.value as VerifierSessionState.Connecting

        assert("$TRANSITION_SUCCESSFUL_TO_STATE ProcessingEngagement" in logger)
        assert("$TRANSITION_SUCCESSFUL_TO_STATE $currentState" in logger)
    }

    @Test
    fun `Session establishment failure means that engagement cannot be processed`() = runTest {
        fakeCryptoService.exceptionToThrow = Exception()
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        orchestrator.verifierSessionState.test {
            assertThat(
                expectMostRecentItem(),
                isFailed(
                    hasReason(
                        instanceOf(SessionErrorReason.CannotProcessEngagement::class.java)
                    )
                )
            )
        }
    }

    @Test
    fun `Null crypto contexts fail QR processing with Service UUID not found`() = runTest {
        verifierCryptoService = DeferredVerifierCryptoService()
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        orchestrator.verifierSessionState.test {
            assertThat(
                expectMostRecentItem(),
                isFailed(
                    hasReason(
                        instanceOf(SessionErrorReason.ServiceUuidNotFound::class.java)
                    )
                )
            )
        }
    }

    @Test
    fun `processQrCode returns invalid BarcodeDataResult`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()
        val data = "https://"

        orchestrator.processQrCode(data)

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed()
        )
    }

    @Test
    fun `processQrCode with null barcode does nothing`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        orchestrator.processQrCode(null)

        assertThat(
            orchestrator.verifierSessionState.value,
            isReadyToScan()
        )
    }

    @Test
    fun `processQrCode with empty barcode does nothing`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        orchestrator.processQrCode("")

        assertThat(
            orchestrator.verifierSessionState.value,
            isReadyToScan()
        )
    }

    @Test
    fun `processQrCode does nothing when session is in an invalid state for scanning`() = runTest {
        initialStates[0] = VerifierSessionState.Verifying
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }

        orchestrator.processQrCode(VALID_MDOC_URI)

        assertThat(
            orchestrator.verifierSessionState.value,
            equalTo(VerifierSessionState.Verifying)
        )
    }

    @Test
    fun `bluetooth disconnection transitions to Failed`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Disconnected("address", false)
        )

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed()
        )
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `bluetooth session end disconnection does not transition to Failed`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Disconnected("address", true)
        )

        assertThat(
            orchestrator.verifierSessionState.value,
            not(isFailed())
        )
    }

    @Test
    fun `bluetooth error transitions to Failed and stops transport`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Error(CentralBluetoothTransportError.SCAN_FAILED)
        )

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed()
        )
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `cancel stops bluetooth transport`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }

        orchestrator.cancel()

        assertThat(
            orchestrator.verifierSessionState.value,
            isCancelled()
        )
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `ConnectionStateStarted encrypts DeviceRequest with counter 1 and increments to 2`() =
        runTest {
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.processQrCode(VALID_MDOC_URI)
            centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

            assertEquals(1u, fakeCryptoService.lastEncryptCounter)
            val context = sessionFactory.getCurrentSession().cryptoContext
            assertEquals(2u, context?.encryptCounter)
        }

    @Test
    fun `AC4 - encryption failure transitions to Failed with CannotEncryptDeviceRequest reason`() =
        runTest {
            val failingCryptoService = FakeVerifierCryptoService().apply {
                buildAndEncryptException = EncryptDeviceRequestException(
                    "Error encrypting DeviceRequest",
                    RuntimeException("AES failure")
                )
            }
            val orchestrator = VerifierOrchestrator(
                logger = logger,
                prerequisiteGate = gate,
                sessionFactory = sessionFactory,
                verifierConfig = photoAndAgeOver21Config,
                centralBluetoothTransport = centralBluetoothTransport,
                appCoroutineScope = scope,
                barcodeParser = FakeQrParser(),
                verifierCryptoService = failingCryptoService
            )
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.processQrCode(VALID_MDOC_URI)
            centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

            assertThat(
                orchestrator.verifierSessionState.value,
                isFailed(
                    hasReason(instanceOf(SessionErrorReason.CannotEncryptDeviceRequest::class.java))
                )
            )
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `counter is not incremented when encryption fails`() = runTest {
        val failingCryptoService = FakeVerifierCryptoService().apply {
            buildAndEncryptException = EncryptDeviceRequestException(
                "Error encrypting DeviceRequest",
                RuntimeException("AES failure")
            )
        }
        val orchestrator = VerifierOrchestrator(
            logger = logger,
            prerequisiteGate = gate,
            sessionFactory = sessionFactory,
            verifierConfig = nameRetainAndAgeOver18Config,
            centralBluetoothTransport = centralBluetoothTransport,
            appCoroutineScope = scope,
            barcodeParser = FakeQrParser(),
            verifierCryptoService = failingCryptoService
        )
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

        val context = sessionFactory.getCurrentSession().cryptoContext
        assertEquals(1u, context?.encryptCounter)
    }

    @Test
    fun `eReaderKey and encryptedDeviceRequest passed to buildSessionEstablishment`() = runTest {
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

        assertArrayEquals(byteArrayOf(), fakeCryptoService.lastEReaderKeyBytes)
        assertArrayEquals(
            fakeCryptoService.buildAndEncryptToReturn,
            fakeCryptoService.lastEncryptedDeviceRequest
        )
    }

    @Test
    fun `SessionEstablishment failure transitions to Failed with reason`() = runTest {
        val failingCryptoService = FakeVerifierCryptoService().apply {
            buildSessionEstablishmentException = SessionEstablishmentException(
                "error constructing SessionEstablishment message",
                RuntimeException("encoding failure")
            )
        }
        val orchestrator = VerifierOrchestrator(
            logger = logger,
            prerequisiteGate = gate,
            sessionFactory = sessionFactory,
            verifierConfig = verifierConfigStub,
            centralBluetoothTransport = centralBluetoothTransport,
            appCoroutineScope = scope,
            barcodeParser = FakeQrParser(),
            verifierCryptoService = failingCryptoService
        )
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed(
                hasReason(
                    instanceOf(SessionErrorReason.CannotBuildSessionEstablishment::class.java)
                )
            )
        )
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `sendMessage success completes transmission`() = runTest {
        centralBluetoothTransport.sendMessageToReturn = true
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

        assertArrayEquals(
            fakeCryptoService.buildSessionEstablishmentToReturn,
            centralBluetoothTransport.lastSentData
        )
    }

    @Test
    fun `sendMessage failure transitions to Failed with CannotSendMessage reason`() = runTest {
        centralBluetoothTransport.sendMessageToReturn = false
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed(hasReason(instanceOf(SessionErrorReason.CannotSendMessage::class.java)))
        )
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    /**
     * DCMAW-19310: AC1: Route a SessionData envelope containing a payload
     */
    @Test
    fun `Serializes SessionData from central bluetooth message`() = runTest {
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        orchestrator.hashCode()
        advanceUntilIdle()

        assertTrue {
            "Deserialized SessionData from bluetooth central Message" in logger
        }
    }

    /**
     * DCMAW-19310: AC2: Handle SessionData reporting a session-level transport error
     */
    @Test
    fun `Navigates to failure state due to SessionDataStatus`(
        @TestParameter status: SessionDataStatus
    ) = runTest {
        fakeCryptoService.sessionData = SessionData(status = status)
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        orchestrator.hashCode()
        advanceUntilIdle()

        assertInvalidSessionDataInstance()
    }

    /**
     * DCMAW-19310: AC3: Handle a SessionData envelope missing a payload
     */
    @Test
    fun `Navigates to failure state due to missing SessionData payload`() = runTest {
        fakeCryptoService.sessionData = SessionData()

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        orchestrator.hashCode()
        advanceUntilIdle()

        assertInvalidSessionDataInstance()
    }

    /**
     * DCMAW-19310: AC4: Handle malformed CBOR during SessionData decoding
     */
    @Test
    fun `Navigates to failure state due to SessionData CBOR encoding exception`() = runTest {
        fakeCryptoService.exceptionToThrow = JsonMappingException.from(
            CborMapper.default.createParser(byteArrayOf()),
            "This is a unit test"
        )

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        orchestrator.hashCode()
        advanceUntilIdle()

        assertInvalidSessionDataInstance()
    }

    private suspend fun assertInvalidSessionDataInstance() {
        assertTrue {
            "Received invalid SessionData instance" in logger
        }

        orchestrator.verifierSessionState.test {
            assertThat(
                expectMostRecentItem(),
                isFailed(
                    hasReason(
                        equalTo(SessionErrorReason.InvalidSessionDataPayload)
                    )
                )
            )
        }
    }
}
