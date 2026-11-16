package uk.gov.onelogin.sharing.holder.consent.moreaboutdvs

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.logging.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck
import uk.gov.onelogin.sharing.holder.consent.HolderConsentUiState
import uk.gov.onelogin.sharing.holder.consent.HolderConsentViewModel
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.prerequisites.api.state.BluetoothState
import uk.gov.onelogin.sharing.prerequisites.impl.MissingPrerequisites

@RunWith(AndroidJUnit4::class)
class MoreAboutDvsScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            MoreAboutDvsContent(
                state = HolderConsentUiState.MoreAboutDvs(
                    normalisedOrganizationName = "Stub-DVS Ltd.",
                    privacyPolicyUrl = "https://this.is.an.accessibility.test/"
                ),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
