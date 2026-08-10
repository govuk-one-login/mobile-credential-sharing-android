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
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
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
    fun `presenter is initially null`() {
        assertNull(viewModel.presenter.value)
    }

    @Test
    fun `getPresenter creates presenter on first call`() = runTest {
        val credential = mockk<MockCredential>()
        val expectedPresenter = mockk<CredentialPresenter>()

        viewModel.getPresenter(credential) { expectedPresenter }

        advanceUntilIdle()

        assertNotNull(viewModel.presenter.value)
        assertEquals(expectedPresenter, viewModel.presenter.value)
    }

    @Test
    fun `getPresenter does not recreate presenter on subsequent calls`() = runTest {
        val credential = mockk<MockCredential>()
        val firstPresenter = mockk<CredentialPresenter>()
        val secondPresenter = mockk<CredentialPresenter>()

        viewModel.getPresenter(credential) { firstPresenter }
        advanceUntilIdle()

        viewModel.getPresenter(credential) { secondPresenter }
        advanceUntilIdle()

        assertEquals(firstPresenter, viewModel.presenter.value)
    }

    @Test
    fun `factory is only called once`() = runTest {
        val credential = mockk<MockCredential>()
        val presenter = mockk<CredentialPresenter>()
        var factoryCallCount = 0

        val factory: (MockCredential) -> CredentialPresenter = {
            factoryCallCount++
            presenter
        }

        viewModel.getPresenter(credential, factory)
        advanceUntilIdle()

        viewModel.getPresenter(credential, factory)
        advanceUntilIdle()

        assertEquals(1, factoryCallCount)
    }
}
