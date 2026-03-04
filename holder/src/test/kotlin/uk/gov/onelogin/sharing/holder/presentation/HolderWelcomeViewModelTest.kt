package uk.gov.onelogin.sharing.holder.presentation

import androidx.lifecycle.SavedStateHandle
import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.BluetoothUiErrorTypes
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.FakeMdocPeripheralTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralState
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralTransport
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc.MdocPeripheralTransportError
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.SessionEndStates
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.security.engagement.FakeEngagementGenerator
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@Ignore
@OptIn(ExperimentalCoroutinesApi::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val logger = SystemLogger()
    private val dummyEngagementData = "ENGAGEMENT_DATA"

    private fun createViewModel(
        mdocPeripheralTransport: MdocPeripheralTransport = FakeMdocPeripheralTransport(),
        engagementGenerator: Engagement = FakeEngagementGenerator(data = dummyEngagementData),
        sessionSecurity: SessionSecurity = FakeSessionSecurity(),
        orchestrator: FakeOrchestrator = FakeOrchestrator()
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        sessionSecurity = sessionSecurity,
        engagementGenerator = engagementGenerator,
        logger = logger,
        savedStateHandle = SavedStateHandle(),
        orchestrator = orchestrator
    )

    @Test
    fun `initially has default state`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNotNull(state.qrData)
        assertEquals(MdocPeripheralState.Idle, state.sessionState)
        assertNull(state.lastErrorMessage)
        assertNotNull(state.uuid)
    }

    @Test
    fun `sets qr code data when key is generated`() = runTest {
        val fakeSessionSecurity = FakeSessionSecurity()
        val viewModel = createViewModel(sessionSecurity = fakeSessionSecurity)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("${Engagement.QR_CODE_SCHEME}$dummyEngagementData", state.qrData)
        assertEquals(MdocPeripheralState.Idle, state.sessionState)
    }

    /*@Test
    fun `collects advertiser state changes`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(initialState = MdocPeripheralState.AdvertisingStarted)
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(MdocPeripheralState.AdvertisingStarted, viewModel.uiState.value.sessionState)

        fakeMdocSession.emitState(MdocPeripheralState.AdvertisingStarted)

        advanceUntilIdle()
        assertEquals(MdocPeripheralState.AdvertisingStarted, viewModel.uiState.value.sessionState)
    }*/

    /*@Test
    fun `stop advertising calls stop and updates state`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(initialState = MdocPeripheralState.AdvertisingStarted)
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocPeripheralState.AdvertisingStarted,
            viewModel.uiState.value.sessionState
        )

        viewModel.stopAdvertising()
        advanceUntilIdle()

        assertEquals(1, fakeMdocSession.stopCalls)
        assertEquals(
            MdocPeripheralState.AdvertisingStopped,
            viewModel.uiState.value.sessionState
        )
    }*/

    @Test
    fun `state updates to connected`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(initialState = MdocPeripheralState.AdvertisingStarted)
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocPeripheralState.AdvertisingStarted,
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocPeripheralState.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to disconnected`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocPeripheralState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocPeripheralState.Disconnected(DEVICE_ADDRESS, false))
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.AdvertisingStopped,
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to error`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocPeripheralState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.ADVERTISING_FAILED
            )
        )
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.Error(MdocPeripheralTransportError.ADVERTISING_FAILED),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.GATT_NOT_AVAILABLE
            )
        )
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.Error(MdocPeripheralTransportError.GATT_NOT_AVAILABLE),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.BLUETOOTH_PERMISSION_MISSING
            )
        )
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.Error(MdocPeripheralTransportError.BLUETOOTH_PERMISSION_MISSING),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(
            MdocPeripheralState.Error(
                MdocPeripheralTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED
            )
        )
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.Error(MdocPeripheralTransportError.DESCRIPTOR_WRITE_REQUEST_FAILED),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to service added`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)
        val uuid = UUID.randomUUID()

        advanceUntilIdle()
        assertEquals(
            MdocPeripheralState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocPeripheralState.ServiceAdded(uuid))
        advanceUntilIdle()

        assertEquals(
            MdocPeripheralState.ServiceAdded(uuid),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to idle`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocPeripheralState.Idle,
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `bluetooth switched off updates state to Disabled`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.OFF)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Disabled,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `bluetooth turning off updates state to Disabled`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.TURNING_OFF)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Disabled,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `bluetooth turning on updates state to Initializing`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.TURNING_ON)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Initializing,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `bluetooth on updates state to Enabled and triggers start BLE session`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        viewModel.updateBluetoothPermissions(true)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Enabled,
            viewModel.uiState.value.bluetoothState
        )

        assertEquals(1, fakeMdocSession.startCalls)
    }

    @Test
    fun `does not start BLE session if permissions not granted`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        viewModel.updateBluetoothPermissions(false)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Enabled,
            viewModel.uiState.value.bluetoothState
        )

        assertEquals(0, fakeMdocSession.startCalls)
    }

    @Test
    fun `bluetooth unknown status on updates state to Unknown`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.UNKNOWN)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Unknown,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `updateBluetoothPermissions should update hasBluetoothPermissions`() {
        val viewModel = createViewModel()

        viewModel.updateBluetoothPermissions(true)

        assertEquals(true, viewModel.uiState.value.hasBluetoothPermissions)
    }

    @Test
    fun `bluetooth ON only triggers start once while already enabled`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        viewModel.updateBluetoothPermissions(true)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)
        advanceUntilIdle()

        assertEquals(
            BluetoothState.Enabled,
            viewModel.uiState.value.bluetoothState
        )
        assertEquals(1, fakeMdocSession.startCalls)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)
        advanceUntilIdle()

        assertEquals(
            BluetoothState.Enabled,
            viewModel.uiState.value.bluetoothState
        )
        assertEquals(
            1,
            fakeMdocSession.startCalls
        )
    }

    @Test
    fun `bluetooth ON does not trigger restart until session has fully stopped`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport()
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        viewModel.updateBluetoothPermissions(true)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)
        advanceUntilIdle()
        assertEquals(1, fakeMdocSession.startCalls)

        fakeMdocSession.emitState(MdocPeripheralState.AdvertisingStopped)
        advanceUntilIdle()

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)
        advanceUntilIdle()

        assertEquals(
            1,
            fakeMdocSession.startCalls
        )
    }

    @Test
    fun `showErrorScreen set to true when mdoc session disconnects`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(initialState = MdocPeripheralState.AdvertisingStarted)
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        advanceUntilIdle()

        fakeMdocSession.emitState(MdocPeripheralState.Disconnected("123123", false))

        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.showErrorScreen)
    }

    @Test
    fun `bluetooth permissions granted initially and sets previouslyHadPermissions true`() =
        runTest {
            val viewModel = createViewModel()

            viewModel.updateBluetoothPermissions(granted = true)

            val state = viewModel.uiState.value

            assertTrue(state.hasBluetoothPermissions!!)
            assertTrue(state.previouslyHadPermissions)
            assertFalse(state.showErrorScreen)
        }

    @Test
    fun `bluetooth permissions revoked and error screen shown`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateBluetoothPermissions(granted = true)
        assertTrue(viewModel.uiState.value.previouslyHadPermissions)

        viewModel.updateBluetoothPermissions(granted = false)

        val state = viewModel.uiState.value

        assertFalse(state.hasBluetoothPermissions!!)
        assertTrue(state.previouslyHadPermissions)
        assertTrue(state.showErrorScreen)
        assertEquals(BluetoothUiErrorTypes.PERMISSIONS_MISSING, state.bluetoothErrorType)
    }

    @Test
    fun `error should not be shown if permissions initially not granted on start up`() = runTest {
        val viewModel = createViewModel()

        assertFalse(viewModel.uiState.value.previouslyHadPermissions)

        viewModel.updateBluetoothPermissions(granted = false)

        val state = viewModel.uiState.value

        assertFalse(state.hasBluetoothPermissions!!)
        assertFalse(state.previouslyHadPermissions)
        assertFalse(state.showErrorScreen)
    }

    @Test
    fun `onScreenDisposed notifies session manager to end the session`() = runTest {
        val fakeMdocSession = FakeMdocPeripheralTransport(
            initialState = MdocPeripheralState.AdvertisingStarted
        )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        val currentUuid = viewModel.uiState.value.uuid
        assertNotNull(currentUuid)

        viewModel.onScreenDisposed()

        assertEquals(
            "The session manager should receive the UUID from the UI state",
            currentUuid,
            fakeMdocSession.lastUuid
        )
    }

    @Test
    fun `shows error screen when a force disconnect occurs`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitState(MdocPeripheralState.Disconnected(DEVICE_ADDRESS, false))
        advanceUntilIdle()

        assertEquals(
            true,
            viewModel.uiState.value.showErrorScreen
        )
    }

    @Test
    fun `shows no error screen when session end causes disconnect`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitState(MdocPeripheralState.Disconnected(DEVICE_ADDRESS, true))
        advanceUntilIdle()

        assertEquals(
            false,
            viewModel.uiState.value.showErrorScreen
        )
    }

    @Test
    fun `logs end session event when session ends`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitState(
            MdocPeripheralState.MdocPeripheralEnded(
                SessionEndStates.SUCCESS
            )
        )
        advanceUntilIdle()

        assert("Mdoc - Ending session" in logger)
    }

    @Test
    fun `shows error when fails to end session`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitState(
            MdocPeripheralState.MdocPeripheralEnded(
                SessionEndStates.NOTIFY_CLIENT_FAILED
            )
        )
        advanceUntilIdle()

        assertEquals(
            true,
            viewModel.uiState.value.showErrorScreen
        )
    }

    @Test
    fun `decrypts device request when session ends`() = runTest {
        val fakeMdocSession =
            FakeMdocPeripheralTransport(
                initialState = MdocPeripheralState.Connected(DEVICE_ADDRESS)
            )
        val viewModel = createViewModel(mdocPeripheralTransport = fakeMdocSession)

        fakeMdocSession.emitState(
            MdocPeripheralState.MessageReceived(
                byteArrayOf(1, 2, 3)
            )
        )
    }

    @Test
    fun `when orchestrator state is PresentingEngagement, should set QR data to ui state`() {
        val orchestrator = FakeOrchestrator(
            initialHolderState = MutableStateFlow(
                HolderSessionState.PresentingEngagement(qrData = "fakeQrData")
            )
        )

        val viewModel = createViewModel(orchestrator = orchestrator)

        assertEquals("fakeQrData", viewModel.uiState.value.qrData)
    }
}
