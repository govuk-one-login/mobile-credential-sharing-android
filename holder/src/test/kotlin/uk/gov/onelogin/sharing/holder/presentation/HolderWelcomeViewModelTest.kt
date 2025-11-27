package uk.gov.onelogin.sharing.holder.presentation

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
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
        mdocBleSession = mdocSessionManager,
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
        assertEquals("mdoc:$dummyEngagementData", state.qrData)
        assertEquals(MdocSessionState.Idle, state.sessionState)
    }

    @Test
    fun `collects advertiser state changes`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager(initialState = MdocSessionState.Starting)
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(MdocSessionState.Starting, viewModel.uiState.value.sessionState)

        fakeMdocSession.emitState(MdocSessionState.Started)

        advanceUntilIdle()
        assertEquals(MdocSessionState.Started, viewModel.uiState.value.sessionState)
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
            MdocSessionState.Started,
            viewModel.uiState.value.sessionState
        )
        assertNull(viewModel.uiState.value.lastErrorMessage)
    }

    @Test
    fun `stop advertising calls stop and updates state`() = runTest {
        val fakeMdocSession = FakeMdocSessionManager(initialState = MdocSessionState.Started)
        val viewModel = createViewModel(mdocSessionManager = fakeMdocSession)

        advanceUntilIdle()
        assertEquals(
            MdocSessionState.Started,
            viewModel.uiState.value.sessionState
        )

        viewModel.stopAdvertising()
        advanceUntilIdle()

        assertEquals(1, fakeMdocSession.stopCalls)
        assertEquals(
            MdocSessionState.Stopped,
            viewModel.uiState.value.sessionState
        )
    }
}
