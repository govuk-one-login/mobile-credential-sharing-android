package uk.gov.onelogin.sharing.holder.presentation

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.engagement.EngagementTimingPolicy
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val qrData = "QR code"
    private var initialHolderState: HolderSessionState = HolderSessionState.PresentingEngagement(
        qrData
    )

    // deadline 15ms, grace 5ms -> qrVisibleDuration = 10ms.
    private val timingPolicy = EngagementTimingPolicy(
        deadline = 15.milliseconds,
        graceDelay = 5.milliseconds
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            parser = FakeQrParser(),
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            orchestrator = orchestrator,
            analyticsLogger = SystemAnalyticsLogger(SystemLogger()),
            dispatcher = mainDispatcherRule.testDispatcher,
            timingPolicy = timingPolicy
        )
    }

    @Test
    fun `Shows the QR code during the 'PresentingEngagement' session state`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            val visible = awaitItem()
            assertThat(visible.qrData, equalTo(qrData))
            assertThat(visible.showQr, equalTo(true))
        }
    }

    @Test
    fun `Hides the QR behind a spinner once the cosmetic display window elapses`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem().showQr, equalTo(true))

            val hidden = awaitItem()
            assertThat(hidden.qrData, equalTo(qrData))
            assertThat(hidden.showQr, equalTo(false))
        }
    }

    @Test
    fun `Has no QR data for inapplicable session states`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        initialHolderState = HolderSessionState.NotStarted

        viewModel.uiState.test {
            val item = awaitItem()
            assertThat(item.qrData, nullValue(String::class.java))
            assertThat(item.showQr, equalTo(false))
        }
    }
}
