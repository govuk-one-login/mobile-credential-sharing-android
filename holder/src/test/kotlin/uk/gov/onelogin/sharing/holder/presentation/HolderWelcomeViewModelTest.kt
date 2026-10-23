package uk.gov.onelogin.sharing.holder.presentation

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
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
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(TestParameterInjector::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val qrData = "QR code"
    private var initialHolderState: HolderSessionState = HolderSessionState.PresentingEngagement(
        qrData
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            parser = FakeQrParser(),
            initialHolderState = MutableStateFlow(initialHolderState)
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
    fun `Provides QR data during the 'PresentingEngagement' session state`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(
                awaitItem().qrData,
                equalTo(qrData)
            )
        }
    }

    @Test
    fun `Has no QR data for inapplicable session states`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        initialHolderState = HolderSessionState.NotStarted

        viewModel.uiState.test {
            assertThat(
                awaitItem().qrData,
                nullValue(String::class.java)
            )
        }
    }

    @Test
    fun `Logs screen view event`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.sendScreenEvent().join()
        assertThat(
            analyticsLogger,
            hasSentEvents(
                contains(HolderWelcomeViewModelTestData.expectedScreenEvent)
            )
        )
    }
}
