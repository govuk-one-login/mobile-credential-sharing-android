package uk.gov.onelogin.sharing.core.presentation.buttons

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionRationaleButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasLaunchedPermission = false

    @Test
    fun standardUsage() = runTest {
        composeTestRule.setContent {
            PermissionRationaleButton(text = "") { hasLaunchedPermission = true }
        }

        composeTestRule.onNodeWithTag("permissionRationaleButton").performClick()
        assertTrue(hasLaunchedPermission)
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            PermissionRationaleButtonPreview()
        }

        composeTestRule.onNodeWithTag("permissionRationaleButton")
            .assertIsDisplayed()
    }
}
