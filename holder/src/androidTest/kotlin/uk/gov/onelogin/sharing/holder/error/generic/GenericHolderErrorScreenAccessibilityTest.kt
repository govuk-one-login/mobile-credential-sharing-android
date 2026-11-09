package uk.gov.onelogin.sharing.holder.error.generic

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme
import uk.gov.onelogin.sharing.core.accessibility.AccessibilityHelper.performAccessibilityCheck

@RunWith(AndroidJUnit4::class)
class GenericHolderErrorScreenAccessibilityTest {
    @get:Rule
    val accessibilityRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun passesAccessibilityChecks() {
        accessibilityRule.performAccessibilityCheck { Render() }
    }

    @Composable
    private fun Render() {
        GdsTheme {
            GenericErrorScreenContent(
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}