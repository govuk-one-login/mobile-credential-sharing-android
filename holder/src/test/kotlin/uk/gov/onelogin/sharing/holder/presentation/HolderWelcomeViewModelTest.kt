package uk.gov.onelogin.sharing.holder.presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.AdvertisingError
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.api.StartAdvertisingException
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.holder.FakeEngagementGenerator
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.holder.util.MainDispatcherRule
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@OptIn(ExperimentalCoroutinesApi::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val dummyEngagementData = "ENGAGEMENT_DATA"

    private fun createViewModel(
        bleAdvertiser: BleAdvertiser = FakeBleAdvertiser(),
        engagementGenerator: Engagement = FakeEngagementGenerator(data = dummyEngagementData),
        sessionSecurity: SessionSecurity = FakeSessionSecurity(publicKey = null)
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        sessionSecurity = sessionSecurity,
        engagementGenerator = engagementGenerator,
        bleAdvertiser = bleAdvertiser,
        dispatcher = mainDispatcherRule.testDispatcher
    )

    @Test
    fun `initially has default state`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertNull(state.qrData)
        assertEquals(AdvertiserState.Idle, state.advertiserState)
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
        assertEquals("mdoc:$dummyEngagementData", state.qrData)
        assertEquals(AdvertiserState.Idle, state.advertiserState)
    }

    @Test
    fun `collects advertiser state changes`() = runTest {
        val fakeBleAdvertiser = FakeBleAdvertiser(initialState = AdvertiserState.Starting)
        val viewModel = createViewModel(bleAdvertiser = fakeBleAdvertiser)

        advanceUntilIdle()
        assertEquals(AdvertiserState.Starting, viewModel.uiState.value.advertiserState)

        fakeBleAdvertiser.emitState(AdvertiserState.Started)

        advanceUntilIdle()
        assertEquals(AdvertiserState.Started, viewModel.uiState.value.advertiserState)
    }

    @Test
    fun `on start advertise success updates state via BleAdvertiser`() = runTest {
        val fakeBleAdvertiser = FakeBleAdvertiser(initialState = AdvertiserState.Idle)
        val viewModel = createViewModel(bleAdvertiser = fakeBleAdvertiser)
        val initialUuid = viewModel.uiState.value.uuid

        viewModel.startAdvertising()

        advanceUntilIdle()
        assertEquals(1, fakeBleAdvertiser.startCalls)
        assertEquals(
            initialUuid,
            fakeBleAdvertiser.lastAdvertiseData?.serviceUuid
        )

        assertEquals(
            AdvertiserState.Started,
            viewModel.uiState.value.advertiserState
        )
        assertNull(viewModel.uiState.value.lastErrorMessage)
    }

    @Test
    fun `start advertising fail sets error message`() = runTest {
        val fakeBleAdvertiser = FakeBleAdvertiser(initialState = AdvertiserState.Idle)
        val viewModel = createViewModel(bleAdvertiser = fakeBleAdvertiser)
        fakeBleAdvertiser.exceptionToThrow = StartAdvertisingException(
            AdvertisingError.BLUETOOTH_DISABLED
        )

        viewModel.startAdvertising()
        advanceUntilIdle()

        assertEquals(
            "Error: Bluetooth is disabled",
            viewModel.uiState.value.lastErrorMessage
        )
        assertEquals(
            AdvertiserState.Idle,
            viewModel.uiState.value.advertiserState
        )
    }

    @Test
    fun `stop advertising calls stop and updates state`() = runTest {
        val fakeBleAdvertiser = FakeBleAdvertiser(initialState = AdvertiserState.Started)
        val viewModel = createViewModel(bleAdvertiser = fakeBleAdvertiser)

        advanceUntilIdle()
        assertEquals(
            AdvertiserState.Started,
            viewModel.uiState.value.advertiserState
        )

        viewModel.stopAdvertising()
        advanceUntilIdle()

        assertEquals(1, fakeBleAdvertiser.stopCalls)
        assertEquals(
            AdvertiserState.Stopped,
            viewModel.uiState.value.advertiserState
        )
    }
}
