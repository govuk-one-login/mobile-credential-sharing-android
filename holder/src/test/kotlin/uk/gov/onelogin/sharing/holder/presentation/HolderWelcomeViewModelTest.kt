package uk.gov.onelogin.sharing.holder.presentation

import java.util.UUID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.ble.DEVICE_ADDRESS
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothStatus
import uk.gov.onelogin.sharing.holder.util.MainDispatcherRule
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.FakeEngagementGenerator
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@OptIn(ExperimentalCoroutinesApi::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dummyEngagementData = "ENGAGEMENT_DATA"

    private fun createViewModel(
        mdocSessionManager: MdocSessionManager = FakeMdocSessionManager(),
        engagementGenerator: Engagement = FakeEngagementGenerator(data = dummyEngagementData),
        sessionSecurity: SessionSecurity = FakeSessionSecurity(publicKey = null)
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        sessionSecurity = sessionSecurity,
        engagementGenerator = engagementGenerator,
        mdocSessionManagerFactory = { mdocSessionManager },
        dispatcher = mainDispatcherRule.testDispatcher,
        logger = SystemLogger()
    )

    @Test
    fun `initially has default state`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.qrData)
        assertEquals(MdocSessionState.Idle, state.sessionState)
        assertNull(state.lastErrorMessage)
        assertNotNull(state.uuid)
    }

    @Test
    fun `sets qr code data when key is generated`() = runTest {
        val dummyPublicKey = SessionSecurityTestStub.generateValidKeyPair()
        val fakeSessionSecurity = FakeSessionSecurity(publicKey = dummyPublicKey)
        val viewModel = createViewModel(sessionSecurity = fakeSessionSecurity)

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("${Engagement.QR_CODE_SCHEME}$dummyEngagementData", state.qrData)
        assertEquals(MdocSessionState.Idle, state.sessionState)
    }

    @Test
    fun `collects advertiser state changes`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.AdvertisingStarted)
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(MdocSessionState.AdvertisingStarted, viewModel.uiState.value.sessionState)

        fakeMdocSession.emitState(MdocSessionState.AdvertisingStarted)

        advanceUntilIdle()
        assertEquals(MdocSessionState.AdvertisingStarted, viewModel.uiState.value.sessionState)
    }

    @Test
    fun `stop advertising calls stop and updates state`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.AdvertisingStarted)
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.AdvertisingStarted,
            viewModel.uiState.value.sessionState
        )

        viewModel.stopAdvertising()
        advanceUntilIdle()

        assertEquals(1, fakeMdocSession.stopCalls)
        assertEquals(
            MdocSessionState.AdvertisingStopped,
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to connected`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.AdvertisingStarted)
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.AdvertisingStarted,
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocSessionState.Connected(DEVICE_ADDRESS))
        advanceUntilIdle()

        assertEquals(
            MdocSessionState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to disconnected`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.Connected(DEVICE_ADDRESS))
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocSessionState.Disconnected(DEVICE_ADDRESS))
        advanceUntilIdle()

        assertEquals(
            MdocSessionState.Disconnected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to error`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.Connected(DEVICE_ADDRESS))
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocSessionState.Error(MdocSessionError.GATT_NOT_AVAILABLE))
        advanceUntilIdle()

        assertEquals(
            MdocSessionState.Error(MdocSessionError.GATT_NOT_AVAILABLE),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to service added`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.Connected(DEVICE_ADDRESS))
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)
        val uuid = UUID.randomUUID()

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Connected(DEVICE_ADDRESS),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocSessionState.ServiceAdded(uuid))
        advanceUntilIdle()

        assertEquals(
            MdocSessionState.ServiceAdded(uuid),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to idle`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Idle,
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `bluetooth switched off updates state to Disabled`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.OFF)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Disabled,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `bluetooth turning off updates state to Disabled`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.TURNING_OFF)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Disabled,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `bluetooth turning on updates state to Initializing`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.TURNING_ON)

        advanceUntilIdle()
        assertEquals(
            BluetoothState.Initializing,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `bluetooth on updates state to Enabled and triggers start BLE session`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

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
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

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
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

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
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

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
        val fakeMdocSession = FakeMdocSessionManager()
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        viewModel.updateBluetoothPermissions(true)

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)
        advanceUntilIdle()
        assertEquals(1, fakeMdocSession.startCalls)

        fakeMdocSession.emitState(MdocSessionState.AdvertisingStopped)
        advanceUntilIdle()

        fakeMdocSession.emitBluetoothState(BluetoothStatus.ON)
        advanceUntilIdle()

        assertEquals(
            1,
            fakeMdocSession.startCalls
        )
    }
}
