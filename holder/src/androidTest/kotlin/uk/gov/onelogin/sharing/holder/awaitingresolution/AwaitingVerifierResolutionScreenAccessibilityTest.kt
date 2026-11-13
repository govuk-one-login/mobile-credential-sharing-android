package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(AndroidJUnit4::class)
class AwaitingVerifierResolutionScreenAccessibilityTest {

    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(
                HolderSessionState.AwaitingVerifierResolution
            )
        )
    }

    private val viewmodel by lazy {
        AwaitingVerifierResolutionViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            orchestrator = orchestrator
        )
    }

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme { AwaitingVerifierResolutionScreen(viewModel = viewmodel) }
    }
}
