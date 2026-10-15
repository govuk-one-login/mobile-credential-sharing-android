package uk.gov.onelogin.sharing.holder.presentation

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
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
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(TestParameterInjector::class)
class HolderWelcomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val qrData = "QR code"

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.PresentingEngagement(qrData)
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            parser = FakeQrParser(),
            initialHolderState = holderState
        )
    }

    private val viewModel by lazy {
        HolderWelcomeViewModel(
            orchestrator = orchestrator,
            analyticsLogger = SystemAnalyticsLogger(SystemLogger()),
            dispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Shows the QR when PresentingEngagement has showQr true`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            val visible = awaitItem()
            assertThat(visible.qrData, equalTo(qrData))
            assertThat(visible.showQr, equalTo(true))
        }
    }

    @Test
    fun `Hides the QR (spinner) when the orchestrator flips showQr to false in place`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem().showQr, equalTo(true))
            holderState.value = HolderSessionState.PresentingEngagement(qrData, showQr = false)

            val hidden = awaitItem()
            assertThat(hidden.qrData, equalTo(qrData))
            assertThat(hidden.showQr, equalTo(false))
        }
    }

    @Test
    fun `Reflects a refreshed engagement with new QR data and showQr true`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem().qrData, equalTo(qrData))
            holderState.value = HolderSessionState.PresentingEngagement(qrData, showQr = false)
            assertThat(awaitItem().showQr, equalTo(false))
            holderState.value = HolderSessionState.PresentingEngagement("new_qr")
            val refreshed = awaitItem()
            assertThat(refreshed.qrData, equalTo("new_qr"))
            assertThat(refreshed.showQr, equalTo(true))
        }
    }

    @Test
    fun `Has no QR data for inapplicable session states`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        holderState.value = HolderSessionState.NotStarted

        viewModel.uiState.test {
            val item = awaitItem()
            assertThat(item.qrData, nullValue(String::class.java))
            assertThat(item.showQr, equalTo(false))
        }
    }
}
