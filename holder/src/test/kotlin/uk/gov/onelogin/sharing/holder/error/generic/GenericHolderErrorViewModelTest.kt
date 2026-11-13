package uk.gov.onelogin.sharing.holder.error.generic

import app.cash.turbine.test
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.contains
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.ViewEvent
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasCancelCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasResetCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasStartCount
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

class GenericHolderErrorViewModelTest {

    val error = SessionError(
        "This is a unit test",
        SessionErrorReason.UnrecoverablePrerequisite()
    )
    private var sessionState: HolderSessionState = HolderSessionState.Complete.Failed(error)

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(sessionState),
            startCount = 1
        )
    }

    private val analyticsLogger = FakeAnalyticsLogger()

    private val viewModel by lazy {
        GenericHolderErrorViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
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
                equalTo(GenericHolderErrorViewModel.NavigationEvent.ExitJourney)
            )
        }
    }

    @Test
    fun `Sends a screen-level event to the analytics logger`() = runTest {
        viewModel.sendScreenEvent(SCREEN_NAME).join()

        assertThat(
            analyticsLogger,
            contains(expectedScreenEvent(error))
        )
    }

    @Test
    fun `A screen-level event without a reason occurs when session error is unavailable`() =
        runTest {
            sessionState = HolderSessionState.NotStarted
            viewModel.sendScreenEvent(SCREEN_NAME).join()

            assertThat(
                analyticsLogger,
                contains(
                    ViewEvent.Error(
                        name = SCREEN_NAME,
                        id = "9bf821af-e8ff-41cf-a311-4b6cec989315",
                        endpoint = "",
                        // malformed reason
                        reason = "",
                        status = "",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
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
