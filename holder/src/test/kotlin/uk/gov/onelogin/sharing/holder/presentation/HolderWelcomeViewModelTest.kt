package uk.gov.onelogin.sharing.holder.presentation

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModelTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.EngagementPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(TestParameterInjector::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val qrData = EngagementPresentation("QR code")

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.PresentingEngagement(qrData)
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            parser = FakeQrParser(),
            initialHolderState = holderState
        )
    }

    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            orchestrator = orchestrator,
            analyticsLogger = analyticsLogger,
            dispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Exposes the presentation from PresentingEngagement`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem(), equalTo(qrData))
        }
    }

    @Test
    fun `Renewal goes from visible to hidden to a fresh QR code`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem(), equalTo(qrData))

            holderState.value = HolderSessionState.PresentingEngagement(
                EngagementPresentation(null)
            )
            val hidden = awaitItem()
            assertNull(hidden.qrPayload)
            assertThat(hidden.cancelRequiresConfirmation, equalTo(false))
        }
    }

    @Test
    fun `Has no QR data for inapplicable session states`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        holderState.value = HolderSessionState.NotStarted

        viewModel.uiState.test {
            val item = awaitItem()
            assertThat(item.qrPayload, nullValue(String::class.java))
            assertThat(item.cancelRequiresConfirmation, equalTo(false))
        }
    }

    @Test
    fun `Logs screen view event`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.sendScreenEvent(SCREEN_NAME).join()
        assertThat(
            analyticsLogger,
            hasSentEvents(
                contains(HolderWelcomeViewModelTestData.expectedScreenEvent)
            )
        )
    }
}
