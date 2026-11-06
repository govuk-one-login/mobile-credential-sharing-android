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
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState

@RunWith(AndroidJUnit4::class)
class BleRestrictedScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(
                HolderSessionState.Preflight(missingPrerequisites = emptyList())
            )
        )
    }

    private val viewModel by lazy {
        BleRestrictedViewModel(
            analyticsLogger = FakeAnalyticsLogger(),
            orchestrator = orchestrator
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
            validator = validator
        ) { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            BleRestrictedScreen(viewModel = viewModel)
        }
    }
}
