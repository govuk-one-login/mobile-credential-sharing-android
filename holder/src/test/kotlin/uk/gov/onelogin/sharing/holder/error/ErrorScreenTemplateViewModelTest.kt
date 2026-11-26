package uk.gov.onelogin.sharing.holder.error

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.error.generic.GenericHolderErrorScreenTestData.expectedGoBackToDocumentsButtonEvent
import kotlin.test.Test

class ErrorScreenTemplateViewModelTest {

    private val analyticsLogger = FakeAnalyticsLogger()

    private val viewModel by lazy {
        ErrorScreenTemplateViewModel(
            analyticsLogger = analyticsLogger
        )
    }

    @Test
    fun `Exiting the journey resets the orchestrator`() = runTest {
        viewModel.navigationEvent.test {
            viewModel.exit().join()

            assertThat(
                expectMostRecentItem(),
                equalTo(NavigationEvent.ExitJourney)
            )
        }
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
