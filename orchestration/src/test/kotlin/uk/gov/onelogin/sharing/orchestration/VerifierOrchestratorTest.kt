package uk.gov.onelogin.sharing.orchestration

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
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
import uk.gov.onelogin.sharing.cameraService.data.BarcodeDataResult
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.data.UriTestData.exampleUriOne
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_ERROR
import uk.gov.onelogin.sharing.orchestration.OrchestratorStubs.LogMessages.START_ORCHESTRATION_SUCCESS
import uk.gov.onelogin.sharing.orchestration.prerequisites.Prerequisite
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteResponse
import uk.gov.onelogin.sharing.orchestration.prerequisites.StubPrerequisiteGate
import uk.gov.onelogin.sharing.orchestration.prerequisites.capability.IncapableReason
import uk.gov.onelogin.sharing.orchestration.session.FakeSessionFactory
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionImpl
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.CompleteVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.data.UncancellableVerifierSessionStates
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.hasMissingPreflightPrerequisites
import uk.gov.onelogin.sharing.orchestration.verifier.session.matchers.VerifierSessionStateMatchers.isCancelled
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

    private var gateResponses: MutableMap<Prerequisite, PrerequisiteResponse> =
        Prerequisite.entries.associateWith {
            PrerequisiteResponse.MeetsPrerequisites
        }.toMutableMap()

    private val gate by lazy {
        StubPrerequisiteGate(gateResponses)
    }

    private val centralBluetoothTransport = FakeCentralBluetoothTransport()

    private val scope = TestScope(mainDispatcherRule.testDispatcher)

    private val orchestrator by lazy {
        VerifierOrchestrator(
            logger = logger,
            prerequisiteGate = gate,
            sessionFactory = sessionFactory,
            verifierConfig = verifierConfigStub,
            centralBluetoothTransport = centralBluetoothTransport,
            appCoroutineScope = scope
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
        gateResponses[Prerequisite.BLUETOOTH] = PrerequisiteResponse.Incapable(
            IncapableReason.MissingHardware
        )

        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()

        assert(START_ORCHESTRATION_SUCCESS in logger)
        assert(START_ORCHESTRATION_ERROR !in logger)

        assertThat(
            orchestrator.verifierSessionState.value,
            allOf(
                hasMissingPreflightPrerequisites(Prerequisite.BLUETOOTH),
                not(hasMissingPreflightPrerequisites(Prerequisite.CAMERA))
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
    fun `processQrCode with valid barcode but no engagement UUID fails`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()
        val barcodeResult = BarcodeDataResult.Valid(exampleUriOne)

        orchestrator.processQrCode(barcodeResult)

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed()
        )
    }

    @Test
    fun `processQrCode returns invalid BarcodeDataResult`() = runTest {
        backgroundScope.launch {
            orchestrator.verifierSessionState.collect {}
        }
        orchestrator.start()
        val data = "https://"
        val barcodeResult = BarcodeDataResult.Invalid(data)

        orchestrator.processQrCode(barcodeResult)

        assertThat(
            orchestrator.verifierSessionState.value,
            isFailed()
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
