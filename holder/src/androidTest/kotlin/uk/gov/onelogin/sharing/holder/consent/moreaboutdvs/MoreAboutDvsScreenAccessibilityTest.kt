package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult
import com.google.android.apps.common.testing.accessibility.framework.checks.TouchTargetSizeCheck
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.android.ui.theme.spacingDouble
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityViewCheckResultMatchers.hasSourceCheckClass
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiState

@RunWith(AndroidJUnit4::class)
class MoreAboutDvsScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    private val validator = AccessibilityValidator().apply {
        setThrowExceptionFor(AccessibilityCheckResult.AccessibilityCheckResultType.WARNING)
        setSuppressingResultMatcher(
            allOf(
                hasSourceCheckClass(TouchTargetSizeCheck::class)
            )
        )
    }

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck(validator) { Render() }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Render() {
        GdsTheme {
            MoreAboutDvsContent(
                state = HolderConsentUiState.Loaded(
                    organizationName = "Stub-DVS Ltd.",
                    normalisedOrganizationName = "Stub-DVS Ltd.",
                    privacyPolicyUrl = "https://this.is.an.accessibility.test/",
                    shouldShowMoreAboutDvs = true
                ),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .padding(top = spacingDouble)
                    .fillMaxSize()
            )
        }
    }
}
