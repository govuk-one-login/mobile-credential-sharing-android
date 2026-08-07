package uk.gov.onelogin.sharing.testapp.holder

import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.sdk.api.presenter.SharingSession
import uk.gov.onelogin.sharing.testapp.credential.MockCredential

@OptIn(ExperimentalCoroutinesApi::class)
class HolderJourneyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: HolderJourneyViewModel

    @Before
    fun setUp() {
        viewModel = HolderJourneyViewModel(dispatcher = mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `session is initially null`() {
        assertNull(viewModel.session.value)
    }

    @Test
    fun `getSession creates session on first call`() = runTest {
        val credential = mockk<MockCredential>()
        val expectedSession = mockk<SharingSession>()

        viewModel.getSession(credential) { expectedSession }

        advanceUntilIdle()

        assertNotNull(viewModel.session.value)
        assertEquals(expectedSession, viewModel.session.value)
    }

    @Test
    fun `getSession does not recreate session on subsequent calls`() = runTest {
        val credential = mockk<MockCredential>()
        val firstSession = mockk<SharingSession>()
        val secondSession = mockk<SharingSession>()

        viewModel.getSession(credential) { firstSession }
        advanceUntilIdle()

        viewModel.getSession(credential) { secondSession }
        advanceUntilIdle()

        assertEquals(firstSession, viewModel.session.value)
    }

    @Test
    fun `factory is only called once`() = runTest {
        val credential = mockk<MockCredential>()
        val session = mockk<SharingSession>()
        var factoryCallCount = 0

        val factory: (MockCredential) -> SharingSession = {
            factoryCallCount++
            session
        }

        viewModel.getSession(credential, factory)
        advanceUntilIdle()

        viewModel.getSession(credential, factory)
        advanceUntilIdle()

        assertEquals(1, factoryCallCount)
    }
}
