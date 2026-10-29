package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
import com.google.android.apps.common.testing.accessibility.framework.checks.DuplicateSpeakableTextCheck
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
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

    private val validator = AccessibilityValidator().apply {
        setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.WARNING)
        setSuppressingResultMatcher(
            object : TypeSafeMatcher<AccessibilityCheckResult>() {
                override fun describeTo(description: Description) {
                    description.appendText("DuplicateSpeakableTextCheck result")
                }

                override fun matchesSafely(item: AccessibilityCheckResult): Boolean =
                    item.sourceCheckClass == DuplicateSpeakableTextCheck::class.java
            }
        )
    }

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck(
            content = { Render() },
            validator = validator
        )
    }

    @Composable
    private fun Render() {
        GdsTheme {
            TurnOnBluetoothScreen(viewModel = viewModel)
        }
    }
}
