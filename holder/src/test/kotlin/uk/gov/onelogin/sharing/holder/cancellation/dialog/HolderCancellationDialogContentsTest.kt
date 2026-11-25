package uk.gov.onelogin.sharing.holder.cancellation.dialog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.empty
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.logging.api.v3.v3dot1.logger.asLegacyEvent
import uk.gov.logging.api.v3dot1.model.TrackEvent.Button
import uk.gov.onelogin.sharing.analytics.RequiredParameterConstants.walletSharingRequiredParameters
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.TestViewModelFactory
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(AndroidJUnit4::class)
class HolderCancellationDialogContentsTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderCancellationDialogContentsRule()

    private val holderState = MutableStateFlow<HolderSessionState>(
        HolderSessionState.ProcessingEstablishment()
    )
    private val orchestrator by lazy {
        FakeOrchestrator(initialHolderState = holderState)
    }
    private val analyticsLogger = FakeAnalyticsLogger()
    private val viewModel by lazy {
        HolderCancellationDialogViewModel(
            orchestrator = orchestrator,
            analyticsLogger = analyticsLogger,
            ioDispatcher = dispatcherRule.testDispatcher
        )
    }

    private val stopTheCheckEvent = Button(
        text = "Stop the check",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    private val continueWithTheCheckEvent = Button(
        text = "Continue with the check",
        params = walletSharingRequiredParameters
    ).asLegacyEvent()

    private val welshCopy = listOf(
        "Ydych chi’n siŵr eich bod eisiau stopio’r gwiriad?",
        "Byddwch yn dod â’ch cysylltiad â’r person sy’n gwneud y gwiriad i ben. " +
            "Ni fydd eich manylion yn cael eu rhannu.",
        "Stopio’r gwiriad",
        "Parhau gyda’r gwiriad"
    )

    @Test
    fun `Validate UI contents`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            assertContentsAreDisplayed()
        }
    }

    @Test
    fun `Continuing with the check dismisses the dialog and logs the button event`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performContinueWithTheCheckClick()
            waitForIdle()

            assertThat(dismissCount, equalTo(1))
        }
        assertThat(analyticsLogger, contains(continueWithTheCheckEvent))
        assertThat(orchestrator.cancelCount, equalTo(0))
    }

    @Test
    fun `Stopping the check before a request arrives cancels the journey`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performStopTheCheckClick()
            waitForIdle()
        }

        assertThat(orchestrator.cancelCount, equalTo(1))
        assertThat(orchestrator.denyConsentCount, equalTo(0))
        assertThat(analyticsLogger, contains(stopTheCheckEvent))
    }

    @Test
    fun `Stopping the check from Agree to share runs the deny behaviour`() = runTest {
        holderState.value = HolderSessionState.AwaitingUserConsent(
            request = mockk(relaxed = true),
            presentation = mockk(relaxed = true)
        )

        composeTestRule.run {
            setContent { Render() }
            performStopTheCheckClick()
            waitForIdle()
        }

        assertThat(orchestrator.denyConsentCount, equalTo(1))
        assertThat(analyticsLogger, contains(stopTheCheckEvent))
    }

    @Config(qualifiers = "cy")
    @Test
    fun `Dialog displays the Welsh translations`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            welshCopy.forEach { onNodeWithText(it).assertIsDisplayed() }
        }
    }

    @Config(qualifiers = "cy")
    @Test
    fun `Analytics for the buttons stay in English when the app is in Welsh`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            onNodeWithText("Parhau gyda’r gwiriad").performClick()
            onNodeWithText("Stopio’r gwiriad").performClick()
            waitForIdle()
        }

        assertThat(analyticsLogger, contains(continueWithTheCheckEvent, stopTheCheckEvent))
    }

    @Test
    fun `Showing the dialog does not log any analytics`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitForIdle()
        }

        assertThat(analyticsLogger.toList(), empty())
    }

    @Test
    fun `Preview follows the same display as the dialog`() = runTest {
        composeTestRule.run {
            setContent { HolderCancellationDialogPreview() }
            assertContentsAreDisplayed()
            performContinueWithTheCheckClick()
            performStopTheCheckClick()
            assertContentsAreDisplayed()
        }
    }

    @Test
    fun `Default arguments resolve the view model from the view model factory`() = runTest {
        val viewModelFactory = TestViewModelFactory(
            viewModelProviders = mapOf(HolderCancellationDialogViewModel::class to { viewModel })
        )

        composeTestRule.run {
            setContent {
                CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                    GdsTheme { HolderCancellationDialogContents() }
                }
            }
            assertContentsAreDisplayed()
            performContinueWithTheCheckClick()
            performStopTheCheckClick()
            waitForIdle()
        }

        assertThat(analyticsLogger, contains(continueWithTheCheckEvent, stopTheCheckEvent))
        assertThat(orchestrator.cancelCount, equalTo(1))
    }

    @Composable
    private fun Render() {
        GdsTheme {
            HolderCancellationDialogContents(
                viewModel = viewModel,
                onDismiss = composeTestRule::incrementDismissCount
            )
        }
    }
}
