package uk.gov.onelogin.sharing.holder.cancellation.dialog

import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Rule
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

class HolderCancellationDialogViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.ProcessingEstablishment
    )
    private val orchestrator = FakeOrchestrator(initialHolderState = holderState)
    private val analyticsLogger = FakeAnalyticsLogger()

    private val viewModel by lazy {
        HolderCancellationDialogViewModel(
            orchestrator = orchestrator,
            analyticsLogger = analyticsLogger,
            ioDispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Stopping the check cancels the journey when there is nothing to deny`() = runTest {
        assertThat(orchestrator.cancelCount, equalTo(0))

        viewModel.stopTheCheck().join()

        assertThat(orchestrator.cancelCount, equalTo(1))
        assertThat(orchestrator.denyConsentCount, equalTo(0))
    }

    @Test
    fun `Stopping the check on Agree to share denies the request`() = runTest {
        holderState.value = HolderSessionState.AwaitingUserConsent(
            request = mockk(relaxed = true),
            presentation = mockk(relaxed = true)
        )

        viewModel.stopTheCheck().join()

        assertThat(orchestrator.denyConsentCount, equalTo(1))
    }

    @Test
    fun `Stopping the check logs the stop the check button`() = runTest {
        viewModel.stopTheCheck().join()

        assertThat(
            analyticsLogger,
            contains(
                Button(
                    text = "Stop the check",
                    params = walletSharingRequiredParameters
                ).asLegacyEvent()
            )
        )
    }

    @Test
    fun `Continuing with the check logs the continue button and does not cancel`() = runTest {
        viewModel.continueWithTheCheck().join()

        assertThat(
            analyticsLogger,
            contains(
                Button(
                    text = "Continue with the check",
                    params = walletSharingRequiredParameters
                ).asLegacyEvent()
            )
        )
        assertThat(orchestrator.cancelCount, equalTo(0))
        assertThat(orchestrator.denyConsentCount, equalTo(0))
    }
}
