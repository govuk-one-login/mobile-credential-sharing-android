package uk.gov.onelogin.sharing.holder.qrcode

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionStateStubs

@RunWith(AndroidJUnit4::class)
class QrCodeScreenAccessibilityTest {

    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private val initialHolderState = MutableStateFlow(
        HolderSessionStateStubs.presentingEngagementStub
    )

    private val viewModel = QrCodeViewModel(
        orchestrator = FakeOrchestrator(initialHolderState = initialHolderState),
        analyticsLogger = FakeAnalyticsLogger()
    )

    @Test
    fun passesAccessibilityChecksWithQrCodeVisible() {
        accessibilityRule.performAccessibilityCheck {
            Render()
        }
    }

    @Test
    fun passesAccessibilityChecksWithQrCodeHidden() {
        initialHolderState.update {
            HolderSessionStateStubs.presentingHiddenEngagementStub
        }
        accessibilityRule.performAccessibilityCheck {
            Render()
        }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            QrCodeScreen(viewModel = viewModel)
        }
    }
}
