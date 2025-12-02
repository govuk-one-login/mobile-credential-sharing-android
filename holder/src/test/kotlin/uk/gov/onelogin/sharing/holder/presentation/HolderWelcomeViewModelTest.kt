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
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionError
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.bluetooth.ble.deviceAddressStub
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
        dispatcher = mainDispatcherRule.testDispatcher
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
    fun `on start advertise success updates state via BleAdvertiser`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager(initialState = MdocSessionState.Idle)
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)
        val initialUuid = viewModel.uiState.value.uuid

        viewModel.startAdvertising()

        advanceUntilIdle()
        assertEquals(1, fakeMdocSession.startCalls)
        assertEquals(
            initialUuid,
            fakeMdocSession.lastUuid
        )

        assertEquals(
            MdocSessionState.AdvertisingStarted,
            viewModel.uiState.value.sessionState
        )
        assertNull(viewModel.uiState.value.lastErrorMessage)
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

        fakeMdocSession.emitState(MdocSessionState.Connected(deviceAddressStub()))
        advanceUntilIdle()

        assertEquals(
            MdocSessionState.Connected(deviceAddressStub()),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to disconnected`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.Connected(deviceAddressStub()))
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Connected(deviceAddressStub()),
            viewModel.uiState.value.sessionState
        )

        fakeMdocSession.emitState(MdocSessionState.Disconnected(deviceAddressStub()))
        advanceUntilIdle()

        assertEquals(
            MdocSessionState.Disconnected(deviceAddressStub()),
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `state updates to error`() = runTest {
        val fakeMdocSession =
            FakeMdocSessionManager(initialState = MdocSessionState.Connected(deviceAddressStub()))
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Connected(deviceAddressStub()),
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
            FakeMdocSessionManager(initialState = MdocSessionState.Connected(deviceAddressStub()))
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)
        val uuid = UUID.randomUUID()

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Connected(deviceAddressStub()),
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
    fun `checkBluetoothStatus should update status to Enabled when bluetooth is on`() {
        val fakeMdocSession = FakeMdocSessionManager().apply {
            isBluetoothEnabled().apply {
                mockBluetoothEnabled = true
            }
        }
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        viewModel.checkBluetoothStatus()

        assertEquals(BluetoothState.Enabled, viewModel.uiState.value.bluetoothStatus)
    }

    @Test
    fun `checkBluetoothStatus should update status to Disabled when bluetooth is off`() {
        val fakeMdocSession = FakeMdocSessionManager().apply {
            isBluetoothEnabled().apply {
                mockBluetoothEnabled = false
            }
        }
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        viewModel.checkBluetoothStatus()

        assertEquals(BluetoothState.Disabled, viewModel.uiState.value.bluetoothStatus)
    }

    @Test
    fun `updateBluetoothPermissions should update hasBluetoothPermissions`() {
        val viewModel = createViewModel()

        viewModel.updateBluetoothPermissions(true)

        assertEquals(true, viewModel.uiState.value.hasBluetoothPermissions)
    }

    @Test
    fun `updateBluetoothState should update the bluetoothStatus`() {
        val viewModel = createViewModel()

        viewModel.updateBluetoothState(BluetoothState.Enabled)

        assertEquals(BluetoothState.Enabled, viewModel.uiState.value.bluetoothStatus)
    }
}
