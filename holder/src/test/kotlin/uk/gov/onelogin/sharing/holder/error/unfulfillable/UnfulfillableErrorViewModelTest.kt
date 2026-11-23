package uk.gov.onelogin.sharing.holder.error.unfulfillable

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.error.NavigationEvent
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.holder.error.unfulfillable.UnfulfillableErrorAnalyticsTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasCancelCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasResetCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasStartCount
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
    fun `Exiting the journey resets the orchestrator`() = runTest {
        viewModel.navigationEvent.test {
            viewModel.reset().join()

            assertThat(
                orchestrator,
                allOf(
                    hasCancelCount(0),
                    hasResetCount(1),
                    hasStartCount(0)
                )
            )

            assertThat(
                expectMostRecentItem(),
                equalTo(NavigationEvent.ExitJourney)
            )
        }
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

    @Test
    fun `Sends an analytics event for button presses`() = runTest {
        viewModel.sendButtonEvent(BUTTON_TEXT).join()

        assertThat(
            analyticsLogger,
            contains(expectedGoBackToDocumentsButtonEvent)
        )
    }
}