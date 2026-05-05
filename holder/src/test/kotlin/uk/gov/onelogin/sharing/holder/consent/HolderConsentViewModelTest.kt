package uk.gov.onelogin.sharing.holder.consent

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.DeviceRequestStub.deviceRequestStub
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
class HolderConsentViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.NotStarted
    )

    private val orchestrator = FakeOrchestrator(initialHolderState = holderState)
    private var confirmConsentCount = 0

    private val orchestratorWithConsentTracking = FakeOrchestrator(
        initialHolderState = holderState,
        onConfirmConsent = { confirmConsentCount++ }
    )

    private val viewModel by lazy {
        HolderConsentViewModel(
            orchestrator = orchestrator,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `onAccept calls confirmConsent on orchestrator`() = runTest(dispatcherRule.testDispatcher) {
        val viewModel = HolderConsentViewModel(orchestrator = orchestratorWithConsentTracking)

        viewModel.onAccept().join()

        assertEquals(1, confirmConsentCount)
    }

    @Test
    fun `onDeny causes orchestrator cancellation`() = runTest(dispatcherRule.testDispatcher) {
        val viewModel = HolderConsentViewModel(orchestrator = orchestratorWithConsentTracking)

        viewModel.onDeny().join()

        assertEquals(1, orchestratorWithConsentTracking.cancelCount)
    }

    @Test
    fun `Emits AwaitingUserConsent when orchestrator transitions`() =
        runTest(dispatcherRule.testDispatcher) {
            viewModel.deviceRequest.test {
                assertThat(awaitItem(), nullValue())

                holderState.value = HolderSessionState.AwaitingUserConsent(deviceRequestStub)

                assertThat(awaitItem(), equalTo(deviceRequestStub))
            }
        }
}
