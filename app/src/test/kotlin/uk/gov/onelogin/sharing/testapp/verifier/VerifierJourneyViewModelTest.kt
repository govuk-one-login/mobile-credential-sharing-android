package uk.gov.onelogin.sharing.testapp.verifier

import android.content.Context
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
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerificationRequest
import uk.gov.onelogin.sharing.sdk.api.verifier.VerificationSession

@OptIn(ExperimentalCoroutinesApi::class)
class VerifierJourneyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: VerifierJourneyViewModel

    @Before
    fun setUp() {
        viewModel = VerifierJourneyViewModel(dispatcher = mainDispatcherRule.testDispatcher)
    }

    @Test
    fun `session is initially null`() {
        assertNull(viewModel.session.value)
    }

    @Test
    fun `getSession creates session on first call`() = runTest {
        val context = mockk<Context>()
        val request = mockk<VerificationRequest>()
        val expectedSession = mockk<VerificationSession>()

        viewModel.getSession(context, request) { _, _ -> expectedSession }

        advanceUntilIdle()

        assertNotNull(viewModel.session.value)
        assertEquals(expectedSession, viewModel.session.value)
    }

    @Test
    fun `getSession does not recreate session on subsequent calls`() = runTest {
        val context = mockk<Context>()
        val request = mockk<VerificationRequest>()
        val firstSession = mockk<VerificationSession>()
        val secondSession = mockk<VerificationSession>()

        viewModel.getSession(context, request) { _, _ -> firstSession }
        advanceUntilIdle()

        viewModel.getSession(context, request) { _, _ -> secondSession }
        advanceUntilIdle()

        assertEquals(firstSession, viewModel.session.value)
    }

    @Test
    fun `factory is only called once`() = runTest {
        val context = mockk<Context>()
        val request = mockk<VerificationRequest>()
        val session = mockk<VerificationSession>()
        var factoryCallCount = 0

        val factory: (Context, VerificationRequest) -> VerificationSession = { _, _ ->
            factoryCallCount++
            session
        }

        viewModel.getSession(context, request, factory)
        advanceUntilIdle()

        viewModel.getSession(context, request, factory)
        advanceUntilIdle()

        assertEquals(1, factoryCallCount)
    }
}
