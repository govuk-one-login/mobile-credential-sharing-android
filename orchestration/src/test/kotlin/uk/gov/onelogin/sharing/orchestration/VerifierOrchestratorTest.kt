package uk.gov.onelogin.sharing.orchestration

import app.cash.turbine.test
import com.fasterxml.jackson.databind.JsonMappingException
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.google.testing.junit.testparameterinjector.TestParameterValuesProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
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
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_MDOC_URI
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.cryptoService.verifier.DecryptDeviceResponseException
import uk.gov.onelogin.sharing.cryptoService.verifier.DeferredVerifierCryptoService
import uk.gov.onelogin.sharing.cryptoService.verifier.EncryptDeviceRequestException
import uk.gov.onelogin.sharing.cryptoService.verifier.FakeVerifierCryptoService
import uk.gov.onelogin.sharing.cryptoService.verifier.SessionEstablishmentException
import uk.gov.onelogin.sharing.cryptoService.verifier.VerifierCryptoService
import uk.gov.onelogin.sharing.models.mdoc.cbor.CborMapper
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionData
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataDto.Companion.toDto
import uk.gov.onelogin.sharing.models.mdoc.sessionData.SessionDataStatus
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseStub
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.Status
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionTerminator
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnrecoverablePrerequisite
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnverifiableDocument
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.nameRetainAndAgeOver18Config
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.photoAndAgeOver21Config
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionImpl
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState.Complete.Failed
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CompleteVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.UncancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.SuccessMatchers.hasDocumentCount
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.SuccessMatchers.hasDocuments
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.hasMissingPreflightPrerequisites
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isCancelled
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isConnecting
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isFailed
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isReadyToScan
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isSuccess
import uk.gov.onelogin.sharing.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.prerequisites.api.MissingPrerequisite
import uk.gov.onelogin.sharing.prerequisites.api.Prerequisite
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.verification.document.DocumentVerifier
import uk.gov.onelogin.sharing.verification.format.document.VerifiableDocumentMatchers.hasDocType
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationError
import uk.gov.onelogin.sharing.verification.format.document.result.VerificationResult

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
@Suppress("LargeClass")
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
    private var documentVerifier: DocumentVerifier = DocumentVerifier { _, _ ->
        VerificationResult.Success
    }

    private val sessionTerminator =
        FakeSessionTerminator(centralBluetoothTransport, verifierCryptoService)

    private val scope = TestScope(mainDispatcherRule.testDispatcher)

    private val orchestrator by lazy {
        createOrchestrator()
    }

    private fun createOrchestrator(
        verifierConfig: VerifierConfig = verifierConfigStub,
        cryptoService: VerifierCryptoService = verifierCryptoService
    ) = VerifierOrchestrator(
        logger = logger,
        prerequisiteGate = gate,
        sessionFactory = sessionFactory,
        verifierConfig = verifierConfig,
        centralBluetoothTransport = centralBluetoothTransport,
        appCoroutineScope = scope,
        barcodeParser = FakeQrParser(),
        verifierCryptoService = cryptoService,
        documentVerifier = documentVerifier,
        sessionTerminator = sessionTerminator
    )

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
            MissingPrerequisites.Bluetooth(BluetoothState.PoweredOff)
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
            MissingPrerequisites.Bluetooth(BluetoothState.Unsupported)
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
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
        advanceUntilIdle()
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Disconnected("address", false)
        )
        advanceUntilIdle()
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
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
        advanceUntilIdle()
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Error(CentralBluetoothTransportError.SCAN_FAILED)
        )
        advanceUntilIdle()
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
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
        orchestrator.cancel()
        advanceUntilIdle()
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
            val orchestrator = createOrchestrator(
                verifierConfig = photoAndAgeOver21Config,
                cryptoService = failingCryptoService
            )
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.processQrCode(VALID_MDOC_URI)
            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)
            advanceUntilIdle()
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
        val orchestrator = createOrchestrator(
            verifierConfig = nameRetainAndAgeOver18Config,
            cryptoService = failingCryptoService
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
        val orchestrator = createOrchestrator(cryptoService = failingCryptoService)
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
        advanceUntilIdle()
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)
        advanceUntilIdle()
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
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
        advanceUntilIdle()
        centralBluetoothTransport.emitState(CentralBluetoothState.ConnectionStateStarted)
        advanceUntilIdle()
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
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

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

    /**
     * DCMAW-20270: AC2: The [DeviceResponse] is only emitted when all documents return
     * [VerificationResult.Success]; it is never emitted if any document fails verification.
     */
    @Test
    fun `Transitions to Success when successfully decrypting and verifying a DeviceResponse`() =
        runTest {
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(
                        fakeCryptoService.sessionData.toDto()
                    )
                )
            )

            advanceUntilIdle()

            assertEquals(1u, fakeCryptoService.lastDecryptCounter)

            val context = sessionFactory.getCurrentSession().cryptoContext
            assertEquals(2u, context?.decryptCounter)

            orchestrator.verifierSessionState.test {
                assertThat(
                    expectMostRecentItem(),
                    isSuccess(
                        allOf(
                            hasDocumentCount(1),
                            hasDocuments(
                                contains(
                                    hasDocType("org.iso.18013.5.1.mDL")
                                )
                            )
                        )
                    )
                )
            }

            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `DeviceResponse with error status transitions to Failed - DeviceRequestProcessingError`(
        @TestParameter(valuesProvider = ErrorStatusProvider::class) errorStatus: Status
    ) = runTest {
        fakeCryptoService.decryptDeviceResponseToReturn = DeviceResponse(
            status = errorStatus
        )

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        advanceUntilIdle()

        val failedState =
            orchestrator.verifierSessionState.value as Failed
        val reason = failedState.error.reason as SessionErrorReason.DeviceRequestProcessingError
        assertEquals(errorStatus.code, reason.statusCode)
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `DeviceResponse with null documents transitions to Failed - DocumentNotReturned`() =
        runTest {
            fakeCryptoService.decryptDeviceResponseToReturn = DeviceResponse(
                status = Status.OK,
                documents = null
            )

            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(
                        fakeCryptoService.sessionData.toDto()
                    )
                )
            )

            advanceUntilIdle()

            assertThat(
                orchestrator.verifierSessionState.value,
                isFailed(hasReason(equalTo(SessionErrorReason.DocumentNotReturned)))
            )
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    /**
     * DCMAW-20270: AC1: After a [DeviceResponse] is received, the session transitions to
     * [VerifierSessionState.Verifying] before any call to [DocumentVerifier.verifyDocument] is
     * made.
     *
     * Note that this is the last step before performing document verification.
     */
    @Test
    fun `DeviceResponse with empty documents list transitions to Failed - DocumentNotReturned`() =
        runTest {
            documentVerifier = mockk(relaxed = true)
            fakeCryptoService.decryptDeviceResponseToReturn = DeviceResponse(
                status = Status.OK,
                documents = emptyList()
            )

            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(
                        fakeCryptoService.sessionData.toDto()
                    )
                )
            )

            advanceUntilIdle()

            assertThat(
                orchestrator.verifierSessionState.value,
                isFailed(hasReason(equalTo(SessionErrorReason.DocumentNotReturned)))
            )
            assertEquals(1, centralBluetoothTransport.stopCalls)

            verify(exactly = 0) {
                documentVerifier.verifyDocument(any(), any())
            }
        }

    /**
     * DCMAW-20270: AC2: The [DeviceResponse] is only emitted when all documents return
     * [VerificationResult.Success]; it is never emitted if any document fails verification.
     */
    @Test
    fun `DeviceResponse with multiple documents passes all through to Success`() = runTest {
        val secondDocument = DeviceResponseStub.document.copy(docType = "org.iso.18013.5.1.mID")
        fakeCryptoService.decryptDeviceResponseToReturn = DeviceResponse(
            status = Status.OK,
            documents = listOf(DeviceResponseStub.document, secondDocument)
        )

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        advanceUntilIdle()

        val successState =
            orchestrator.verifierSessionState.value as VerifierSessionState.Complete.Success

        assertThat(
            successState,
            isSuccess(
                allOf(
                    hasDocumentCount(
                        fakeCryptoService.decryptDeviceResponseToReturn.documentCount
                    ),
                    hasDocuments(
                        contains(
                            hasDocType("org.iso.18013.5.1.mDL"),
                            hasDocType("org.iso.18013.5.1.mID")
                        )
                    )
                )
            )
        )
    }

    @Test
    fun `decryption failure transitions to Failed with CannotDecryptDeviceResponse`() = runTest {
        fakeCryptoService.decryptDeviceResponseException =
            DecryptDeviceResponseException("Error decrypting DeviceResponse", RuntimeException())

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        advanceUntilIdle()

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed(
                hasReason(
                    equalTo(SessionErrorReason.CannotDecryptDeviceResponse)
                )
            )
        )
        assertEquals(1, centralBluetoothTransport.stopCalls)

        val context = sessionFactory.getCurrentSession().cryptoContext
        assertEquals(1u, context?.decryptCounter)
    }

    /**
     * DCMAW-20270: AC3: A [DeviceResponse] containing multiple documents fails immediately when
     * the first document produces [VerificationResult.Failure]; remaining documents are not
     * verified.
     *
     * DCMAW-20270: AC6: The [uk.gov.onelogin.sharing.models.mdoc.transcript.SessionTranscript]
     * is passed from the Sharing SDK session context directly to [DocumentVerifier.verifyDocument]
     * without transformation.
     *
     * DCMAW-20270: AC7: When the session transitions to [Failed], the [VerificationError]
     * reason from [VerificationResult.Failure] is available on the Failed state.
     */
    @Test
    fun `Doesn't verify additional documents after a verification failure`(
        @TestParameter error: VerificationError
    ) = runTest {
        documentVerifier = mockk(relaxed = true)
        every {
            documentVerifier.verifyDocument(DeviceResponseStub.document, any())
        } throws VerificationResult.Failure(error)

        val secondDocument = DeviceResponseStub.document.copy(docType = "org.iso.18013.5.1.mID")
        fakeCryptoService.decryptDeviceResponseToReturn = DeviceResponse(
            status = Status.OK,
            documents = listOf(DeviceResponseStub.document, secondDocument)
        )

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(
                    fakeCryptoService.sessionData.toDto()
                )
            )
        )

        advanceUntilIdle()

        orchestrator.verifierSessionState.test {
            assertThat(
                expectMostRecentItem(),
                isFailed(
                    hasReason(
                        isUnverifiableDocument(error)
                    )
                )
            )
        }

        verify(exactly = 1) {
            documentVerifier.verifyDocument(
                DeviceResponseStub.document,
                sessionFactory.getCurrentSession().cryptoContext?.sessionTranscriptBytes
            )
        }
        verify(exactly = 0) {
            documentVerifier.verifyDocument(
                secondDocument,
                sessionFactory.getCurrentSession().cryptoContext?.sessionTranscriptBytes
            )
        }
    }

    @Test
    fun `validation success, no holder status 20, sends termination then transitions to Success`() =
        runTest {
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )

            advanceUntilIdle()

            assertThat(orchestrator.verifierSessionState.value, isSuccess())
            assertEquals(1, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(1, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `validation fails, no holder status 20, sends termination then transitions to Failed`() =
        runTest {
            documentVerifier = DocumentVerifier { _, _ ->
                throw VerificationResult.Failure(VerificationError.INVALID_ISSUER_SIGNATURE)
            }

            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )

            advanceUntilIdle()

            assertThat(
                orchestrator.verifierSessionState.value,
                isFailed(
                    hasReason(isUnverifiableDocument(VerificationError.INVALID_ISSUER_SIGNATURE))
                )
            )
            assertEquals(1, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(1, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `validation succeeds, holder sent status 20, BLE open, skips termination message`() =
        runTest {
            fakeCryptoService.sessionData = SessionData(
                data = fakeCryptoService.sessionData.data,
                status = SessionDataStatus.SESSION_TERMINATION
            )

            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )

            advanceUntilIdle()

            assertThat(orchestrator.verifierSessionState.value, isSuccess())
            assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(0, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `validation fails, holder sent status 20, BLE open, skips termination message`() =
        runTest {
            fakeCryptoService.sessionData = SessionData(
                data = fakeCryptoService.sessionData.data,
                status = SessionDataStatus.SESSION_TERMINATION
            )
            documentVerifier = DocumentVerifier { _, _ ->
                throw VerificationResult.Failure(VerificationError.INVALID_ISSUER_SIGNATURE)
            }

            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )

            advanceUntilIdle()

            assertThat(
                orchestrator.verifierSessionState.value,
                isFailed(
                    hasReason(isUnverifiableDocument(VerificationError.INVALID_ISSUER_SIGNATURE))
                )
            )
            assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(0, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `validation succeeds, holder sent status 20 and closed BLE, ble stop called`() = runTest {
        fakeCryptoService.sessionData = SessionData(
            data = fakeCryptoService.sessionData.data,
            status = SessionDataStatus.SESSION_TERMINATION
        )

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)
        advanceUntilIdle()
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Disconnected("address", isSessionEnd = true)
        )
        advanceUntilIdle()
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
            )
        )

        advanceUntilIdle()

        assertThat(orchestrator.verifierSessionState.value, isSuccess())
        assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
        assertEquals(0, centralBluetoothTransport.sendEndCalls)
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `validation fails, holder sent status 20 and closed BLE, ble stop called`() = runTest {
        fakeCryptoService.sessionData = SessionData(
            data = fakeCryptoService.sessionData.data,
            status = SessionDataStatus.SESSION_TERMINATION
        )
        documentVerifier = DocumentVerifier { _, _ ->
            throw VerificationResult.Failure(VerificationError.INVALID_ISSUER_SIGNATURE)
        }

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)

        advanceUntilIdle()
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Disconnected("address", isSessionEnd = true)
        )
        advanceUntilIdle()
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
            )
        )

        advanceUntilIdle()

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed(
                hasReason(isUnverifiableDocument(VerificationError.INVALID_ISSUER_SIGNATURE))
            )
        )
        assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
        assertEquals(0, centralBluetoothTransport.sendEndCalls)
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `SessionData with data and non-20 code goes to Failed without sending termination`() =
        runTest {
            fakeCryptoService.sessionData = SessionData(
                data = fakeCryptoService.sessionData.data,
                status = SessionDataStatus.ERROR_SESSION_ENCRYPTION
            )

            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )

            advanceUntilIdle()

            assertThat(orchestrator.verifierSessionState.value, isFailed())
            assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(0, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)

            val context = sessionFactory.getCurrentSession().cryptoContext
            assertEquals(1u, context?.decryptCounter)
        }

    @Test
    fun `receiving malformed or non-SessionData in Connecting triggers status 20 and GATT End`() =
        runTest {
            initialStates[0] = VerifierSessionState.Connecting
            val orchestrator = createOrchestrator()
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }

            fakeCryptoService.establishSession(VALID_MDOC_URI) { context ->
                sessionFactory.getCurrentSession().updateCryptoContext { context }
                context
            }

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            advanceUntilIdle()

            fakeCryptoService.sessionData = SessionData(data = null, status = null)
            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )
            advanceUntilIdle()
            assertThat(orchestrator.verifierSessionState.value, isFailed())
            assertEquals(1, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(1, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `SessionData with data and non-20 status in Connecting skips GATT End`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        val orchestrator = createOrchestrator()
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
        advanceUntilIdle()

        fakeCryptoService.sessionData = SessionData(
            data = byteArrayOf(0x01),
            status = SessionDataStatus.ERROR_CBOR_DECODING
        )
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
            )
        )
        advanceUntilIdle()

        assertThat(orchestrator.verifierSessionState.value, isFailed())
        assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
        assertEquals(0, centralBluetoothTransport.sendEndCalls)
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `messages arriving while in Verifying state are ignored`() = runTest {
        initialStates[0] = VerifierSessionState.Verifying
        val orchestrator = createOrchestrator()
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(SERVER_2_CLIENT_UUID, byteArrayOf(0x01))
        )
        advanceUntilIdle()

        assertTrue {
            "Ignoring unexpected message while verifying" in logger
        }

        assertEquals(VerifierSessionState.Verifying, orchestrator.verifierSessionState.value)
        assertEquals(0, centralBluetoothTransport.sendEndCalls)
        assertEquals(0, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `status-only SessionData in Connecting with BLE open sends GATT End but no status 20`() =
        runTest {
            initialStates[0] = VerifierSessionState.Connecting
            val orchestrator = createOrchestrator()
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }

            fakeCryptoService.establishSession(VALID_MDOC_URI) { context ->
                sessionFactory.getCurrentSession().updateCryptoContext { context }
                context
            }

            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))
            advanceUntilIdle()

            fakeCryptoService.sessionData =
                SessionData(data = null, status = SessionDataStatus.ERROR_SESSION_ENCRYPTION)
            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )
            advanceUntilIdle()

            assertThat(orchestrator.verifierSessionState.value, isFailed())
            assertEquals(0, fakeCryptoService.buildTerminationSessionDataCalls)
            assertEquals(0, centralBluetoothTransport.sendEndCalls)
            assertEquals(1, centralBluetoothTransport.stopCalls)
        }

    @Test
    fun `status-only SessionData in Connecting with BLE closed skips GATT End`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        val orchestrator = createOrchestrator()
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Disconnected(
                "address",
                isSessionEnd = true
            )
        )
        advanceUntilIdle()

        fakeCryptoService.sessionData =
            SessionData(data = null, status = SessionDataStatus.SESSION_TERMINATION)
        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
            )
        )
        advanceUntilIdle()

        assertThat(orchestrator.verifierSessionState.value, isFailed())
        assertEquals(0, centralBluetoothTransport.sendEndCalls)
        assertEquals(1, centralBluetoothTransport.stopCalls)
    }

    @Test
    fun `late data during successful validation is ignored and session reaches Success`() =
        runTest {
            backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
            orchestrator.start()
            orchestrator.processQrCode(VALID_MDOC_URI)
            centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(
                    SERVER_2_CLIENT_UUID,
                    CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
                )
            )

            centralBluetoothTransport.emitState(
                CentralBluetoothState.Message(SERVER_2_CLIENT_UUID, byteArrayOf(0x99.toByte()))
            )

            advanceUntilIdle()
            assertThat(orchestrator.verifierSessionState.value, isSuccess())
            assertEquals(1u, fakeCryptoService.lastDecryptCounter)
        }

    @Test
    fun `late data during failing validation is ignored and session reaches Failed`() = runTest {
        documentVerifier = DocumentVerifier { _, _ ->
            throw VerificationResult.Failure(VerificationError.INVALID_ISSUER_SIGNATURE)
        }

        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }
        orchestrator.start()
        orchestrator.processQrCode(VALID_MDOC_URI)
        centralBluetoothTransport.emitState(CentralBluetoothState.Connected("address"))

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(
                SERVER_2_CLIENT_UUID,
                CborMapper.default.writeValueAsBytes(fakeCryptoService.sessionData.toDto())
            )
        )

        centralBluetoothTransport.emitState(
            CentralBluetoothState.Message(SERVER_2_CLIENT_UUID, byteArrayOf(0x99.toByte()))
        )

        advanceUntilIdle()
        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed(hasReason(isUnverifiableDocument(VerificationError.INVALID_ISSUER_SIGNATURE)))
        )
    }

    @Test
    fun `GATT End received in Connecting transitions to Failed`() = runTest {
        initialStates[0] = VerifierSessionState.Connecting
        val orchestrator = createOrchestrator()
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }

        centralBluetoothTransport.emitState(
            CentralBluetoothState.CentralBluetoothEnded(
                SessionEndStates.SUCCESS
            )
        )
        advanceUntilIdle()

        assertThat(orchestrator.verifierSessionState.value, isFailed())
    }

    @Test
    fun `Disconnect in Verifying state is ignored and session continues`() = runTest {

        initialStates[0] = VerifierSessionState.Verifying
        val orchestrator = createOrchestrator()
        backgroundScope.launch { orchestrator.verifierSessionState.collect {} }

        centralBluetoothTransport.emitState(CentralBluetoothState.Disconnected("address", false))
        advanceUntilIdle()

        assertEquals(VerifierSessionState.Verifying, orchestrator.verifierSessionState.value)
        assertTrue { "Ignoring disconnect while verifying" in logger }
    }

    class ErrorStatusProvider : TestParameterValuesProvider() {
        override fun provideValues(context: Context?): List<Status> = listOf(
            Status.GENERAL_ERROR,
            Status.CBOR_DECODING_ERROR,
            Status.CBOR_VALIDATION_ERROR
        )
    }
}
