package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiState

@RunWith(AndroidJUnit4::class)
class MoreAboutDvsScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
