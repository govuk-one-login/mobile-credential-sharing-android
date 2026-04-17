package uk.gov.onelogin.sharing.holder.consent

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isAwaitingUserConsent
import uk.gov.onelogin.sharing.orchestration.holder.session.matchers.HolderSessionStateMatchers.isNotStarted
import uk.gov.onelogin.sharing.orchestration.session.SessionError

@OptIn(ExperimentalCoroutinesApi::class)
class HolderConsentViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.NotStarted
    )

    private val orchestrator = FakeOrchestrator(initialHolderState = holderState)

    private val viewModel by lazy {
        HolderConsentViewModel(
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Exposes orchestrator holder session state`() = runTest(dispatcherRule.testDispatcher) {
        viewModel.holderSessionState.test {
            assertThat(awaitItem(), isNotStarted())
        }
    }

    @Test
    fun `Emits AwaitingUserConsent when orchestrator transitions`() =
        runTest(dispatcherRule.testDispatcher) {
            viewModel.holderSessionState.test {
                assertThat(awaitItem(), isNotStarted())

                holderState.value = HolderSessionState.AwaitingUserConsent(deviceRequestStub)

                assertThat(awaitItem(), isAwaitingUserConsent())
            }
        }

    @Test
    fun `emits NavigateToGenericError when orchestrator transitions to Failed`() =
        runTest(dispatcherRule.testDispatcher) {
            advanceUntilIdle()

            viewModel.navEvents.test {
                holderState.value = HolderSessionState.Complete.Failed(
                    SessionError(
                        message = "encryption failed",
                        exception = RuntimeException("encryption failed")
                    )
                )
                advanceUntilIdle()

                assertEquals(
                    HolderConsentNavEvents.NavigateToGenericError,
                    awaitItem()
                )
            }
        }
}
