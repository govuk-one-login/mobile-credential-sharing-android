package uk.gov.onelogin.sharing.holder.error.unfulfillable

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import kotlin.test.Test

class UnfulfillableErrorViewModelTest {

    private val analyticsLogger = FakeAnalyticsLogger()

    private val initialHolderSessionState: HolderSessionState = HolderSessionState.Complete.Success(
        HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderSessionState)
        )
    }

    private val viewModel by lazy {
        UnfulfillableErrorViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
        )
    }

    @Test
    fun `Sends an error screen analytics event`() = runTest {
        viewModel.sendScreenEvent(SCREEN_NAME).join()

        assertThat(
            analyticsLogger,
            contains(
                expectedScreenEvent(
                    HolderSessionState.Complete.SuccessReason.UnfulfillableRequest
                )
            )
        )
    }
}