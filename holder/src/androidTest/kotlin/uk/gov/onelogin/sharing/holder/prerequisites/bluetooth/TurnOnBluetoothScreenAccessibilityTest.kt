package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

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
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.prerequisites.usecases.ResolveHolderPrerequisiteAction
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites
import uk.gov.onelogin.sharing.prerequisites.permissions.FakePermissionChecker

@RunWith(AndroidJUnit4::class)
class TurnOnBluetoothScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(
                HolderSessionState.Preflight(
                    missingPrerequisites = listOf(
                        MissingPrerequisites.Bluetooth(state = BluetoothState.PoweredOff)
                    )
                )
            )
        )
    }

    private val viewModel by lazy {
        TurnOnBluetoothViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            orchestrator = orchestrator,
            resolver = ResolveHolderPrerequisiteAction(
                logger = MemorisedLogger(),
                orchestrator = orchestrator,
                permissionChecker = FakePermissionChecker()
            )
        )
    }

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            TurnOnBluetoothScreen(viewModel = viewModel)
        }
    }
}
