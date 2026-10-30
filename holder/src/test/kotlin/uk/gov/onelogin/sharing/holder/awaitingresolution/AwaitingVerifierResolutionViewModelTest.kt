package uk.gov.onelogin.sharing.holder.awaitingresolution

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
class AwaitingVerifierResolutionViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val orchestrator = FakeOrchestrator(
        initialHolderState = MutableStateFlow(HolderSessionState.Complete.Success()),
        startCount = 1
    )
    private val analyticsLogger = FakeAnalyticsLogger()

    private val viewModel = AwaitingVerifierResolutionViewModel(analyticsLogger, orchestrator)

    @Test
    fun `Resets the orchestrator`() = runTest(dispatcherRule.testDispatcher) {
        viewModel.reset()
        advanceUntilIdle()
        assertThat(orchestrator.resetCount, equalTo(1))
    }

    @Test
    fun `Logs the screen view event`() {
        viewModel.logScreenView()
        assertThat(
            analyticsLogger,
            contains(AwaitingVerifierResolutionTestData.expectedScreenEvent)

        )
    }

    @Test
    fun `Logs the go back to documents button event`() {
        viewModel.logGoBackToDocuments()
        assertThat(
            analyticsLogger,
            contains(AwaitingVerifierResolutionTestData.expectedGoBackToDocumentsEvent)

        )
    }
}
