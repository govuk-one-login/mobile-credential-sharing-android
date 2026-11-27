package uk.gov.onelogin.sharing.holder.qrcode

import app.cash.turbine.test
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLogger
import uk.gov.onelogin.sharing.analytics.StoredAnalyticsLoggerMatchers.hasSentEvents
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.cryptoService.scanner.FakeQrParser
import uk.gov.onelogin.sharing.holder.qrcode.QrCodeViewModelTestData.SCREEN_NAME
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.EngagementPresentation
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs.engagementPresentationStub

@RunWith(TestParameterInjector::class)
class QrCodeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val visibleQr = QrCodeUiState(
        HolderSessionStateStubs.engagementPresentationStub.qrPayload
    )
    private val hiddenQr = QrCodeUiState(null)
    private val freshQr = QrCodeUiState(FRESH_QR_PAYLOAD)

    private val holderState = MutableStateFlow(
        HolderSessionStateStubs.presentingEngagementStub
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            parser = FakeQrParser(),
            initialHolderState = holderState
        )
    }

    private val analyticsLogger = StoredAnalyticsLogger()

    private val viewModel by lazy {
        QrCodeViewModel(
            orchestrator = orchestrator,
            analyticsLogger = analyticsLogger,
            dispatcher = mainDispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Maps the presentation to UI state`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem(), equalTo(visibleQr))
        }
    }

    @Test
    fun `Renewal goes from visible to hidden to a fresh QR code`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        viewModel.uiState.test {
            assertThat(awaitItem(), equalTo(visibleQr))

            holderState.value = HolderSessionStateStubs.presentingHiddenEngagementStub
            assertThat(awaitItem(), equalTo(hiddenQr))

            holderState.value = HolderSessionState.PresentingEngagement(
                engagementPresentationStub.copy(qrPayload = FRESH_QR_PAYLOAD)
            )
            assertThat(awaitItem(), equalTo(freshQr))
        }
    }

    @Test
    fun `Replacement hides the expired QR code when the hiding update is skipped`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.uiState.test {
                assertThat(awaitItem(), equalTo(visibleQr))

                holderState.value = HolderSessionState.ReadyToPresent
                assertThat(awaitItem(), equalTo(hiddenQr))

                holderState.value = HolderSessionState.PresentingEngagement(
                    engagementPresentationStub.copy(qrPayload = FRESH_QR_PAYLOAD)
                )
                assertThat(awaitItem(), equalTo(freshQr))
            }
        }

    @Test
    fun `Has no QR data for inapplicable session states`() = runTest(
        mainDispatcherRule.testDispatcher
    ) {
        holderState.value = HolderSessionState.NotStarted

        viewModel.uiState.test {
            assertThat(awaitItem(), equalTo(hiddenQr))
        }
    }

    @Test
    fun `Logs screen view event`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.sendScreenEvent(SCREEN_NAME).join()
        assertThat(
            analyticsLogger,
            hasSentEvents(
                contains(QrCodeViewModelTestData.expectedScreenEvent)
            )
        )
    }

    private companion object {
        const val FRESH_QR_PAYLOAD = "Fresh QR code"
    }
}
