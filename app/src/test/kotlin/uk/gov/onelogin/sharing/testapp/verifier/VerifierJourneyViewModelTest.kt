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
import uk.gov.onelogin.sharing.sdk.api.verifier.CredentialVerifier

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
    fun `verifier is initially null`() {
        assertNull(viewModel.verifier.value)
    }

    @Test
    fun `getVerifier creates verifier on first call`() = runTest {
        val context = mockk<Context>()
        val request = mockk<VerificationRequest>()
        val expectedVerifier = mockk<CredentialVerifier>()

        viewModel.getVerifier(context, request) { _, _ -> expectedVerifier }

        advanceUntilIdle()

        assertNotNull(viewModel.verifier.value)
        assertEquals(expectedVerifier, viewModel.verifier.value)
    }

    @Test
    fun `getVerifier does not recreate verifier on subsequent calls`() = runTest {
        val context = mockk<Context>()
        val request = mockk<VerificationRequest>()
        val firstVerifier = mockk<CredentialVerifier>()
        val secondVerifier = mockk<CredentialVerifier>()

        viewModel.getVerifier(context, request) { _, _ -> firstVerifier }
        advanceUntilIdle()

        viewModel.getVerifier(context, request) { _, _ -> secondVerifier }
        advanceUntilIdle()

        assertEquals(firstVerifier, viewModel.verifier.value)
    }

    @Test
    fun `factory is only called once`() = runTest {
        val context = mockk<Context>()
        val request = mockk<VerificationRequest>()
        val verifier = mockk<CredentialVerifier>()
        var factoryCallCount = 0

        val factory: (Context, VerificationRequest) -> CredentialVerifier = { _, _ ->
            factoryCallCount++
            verifier
        }

        viewModel.getVerifier(context, request, factory)
        advanceUntilIdle()

        viewModel.getVerifier(context, request, factory)
        advanceUntilIdle()

        assertEquals(1, factoryCallCount)
    }
}
