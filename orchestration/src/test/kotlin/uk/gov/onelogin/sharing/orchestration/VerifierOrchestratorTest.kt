package uk.gov.onelogin.sharing.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothState
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.CentralBluetoothTransportError
import uk.gov.onelogin.sharing.bluetooth.api.central.mdoc.FakeCentralBluetoothTransport
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.data.UriTestData.mdocExampleUriOne
import uk.gov.onelogin.sharing.cryptoService.DecoderStub.VALID_MDOC_URI
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.cryptoService.verifier.FakeVerifierCryptoService
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.TRANSITION_SUCCESSFUL_TO_STATE
import uk.gov.onelogin.sharing.orchestration.prerequisites.MissingPrerequisiteV2
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorMatchers.hasReason
import uk.gov.onelogin.sharing.orchestration.session.matchers.SessionErrorReasonMatchers.isUnrecoverablePrerequisite
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

    private var gateResponses: MutableList<MissingPrerequisiteV2> = mutableListOf()

    private val gate by lazy {
        StubPrerequisiteGate(gateResponses)
    }

    private val centralBluetoothTransport = FakeCentralBluetoothTransport()
    private val verifierCryptoService = FakeVerifierCryptoService()

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
            MissingPrerequisiteV2.Bluetooth(BluetoothState.PoweredOff)
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
            MissingPrerequisiteV2.Bluetooth(BluetoothState.Unsupported)
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
}
