package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites

@RunWith(AndroidJUnit4::class)
class MoreAboutDvsScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private var missingPrerequisites = mutableListOf(
        MissingPrerequisites.Bluetooth(state = BluetoothState.PermissionNotGranted)
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val initialHolderState: HolderSessionState by lazy {
        HolderSessionState.Preflight(
            missingPrerequisites = missingPrerequisites
        ) { }
    }

    private val analyticsLogger by lazy {
        FakeAnalyticsLogger()
    }

    private val viewModel by lazy {
        MoreAboutDvsViewModel(
            analyticsLogger = analyticsLogger,
            orchestrator = orchestrator,
        )
    }

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            MoreAboutDvsScreen(
                viewModel = viewModel
            )
        }
    }
}
