package uk.gov.onelogin.sharing.holder.error.readerauth

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
import uk.gov.onelogin.sharing.holder.error.NavigationEvent
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorScreenTestData.BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorScreenTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import uk.gov.onelogin.sharing.holder.error.readerauth.ReaderAuthErrorScreenTestData.expectedScreenEvent
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasCancelCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasResetCount
import uk.gov.onelogin.sharing.orchestration.FakeOrchestratorMatchers.hasStartCount
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

class ReaderAuthErrorViewModelTest {

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
        ReaderAuthErrorViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator
        )
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
                        id = "d3e44133-024c-4ce8-84bf-7bed9c01ff96",
                        endpoint = "",
                        // malformed reason
                        reason = "",
                        status = "",
                        params = walletSharingRequiredParameters
                    ).asLegacyEvent()
                )
            )
        }
}
