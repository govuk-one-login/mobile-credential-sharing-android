package uk.gov.onelogin.sharing.holder.awaitingresolution

import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionTestData.BUTTON_TEXT
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionTestData.SCREEN_ID
import uk.gov.onelogin.sharing.holder.awaitingresolution.AwaitingVerifierResolutionTestData.SCREEN_NAME
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
    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel = AwaitingVerifierResolutionViewModel(analyticsLogger, orchestrator)

    @Test
    fun `Dismisses the journey through orchestrator`() {
        viewModel.dismissJourney()
        assertThat(orchestrator.dismissCount, equalTo(1))
        assertThat(orchestrator.resetCount, equalTo(0))
    }

    @Test
    fun `Logs the screen view event`() {
        viewModel.sendScreenEvent(SCREEN_NAME)
        assertThat(
            analyticsLogger,
            contains(AwaitingVerifierResolutionTestData.expectedScreenEvent)

        )
    }

    @Test
    fun `Logs the go back to documents button event`() {
        viewModel.sendGoBackToDocumentsEvent()
        assertThat(
            analyticsLogger,
            contains(AwaitingVerifierResolutionTestData.expectedGoBackToDocumentsEvent)

        )
    }

    @Test
    fun `Screen view event sets name and id`() {
        viewModel.sendScreenEvent(SCREEN_NAME)

        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals("screen_view", event.eventType)
        assertEquals(SCREEN_NAME.lowercase(), event.parameters["screen_name"])
        assertEquals(SCREEN_ID, event.parameters["screen_id"])
    }

    @Test
    fun `Go back to documents button event sets text and type`() {
        viewModel.sendGoBackToDocumentsEvent()

        val event = analyticsLogger.sentAnalyticsEvents.single()
        assertEquals("navigation", event.eventType)
        assertEquals(BUTTON_TEXT.lowercase(), event.parameters["text"])
        assertEquals("submit form", event.parameters["type"])
    }
}
