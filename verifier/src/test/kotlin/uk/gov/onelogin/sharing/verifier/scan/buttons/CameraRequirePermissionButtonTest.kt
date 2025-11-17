package uk.gov.onelogin.sharing.verifier.scan.buttons

import androidx.compose.runtime.Composable
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
class CameraRequirePermissionButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasLaunchedPermission = false

    @Test
    fun standardUsage() = verifyBehaviour {
        CameraRequirePermissionButton { hasLaunchedPermission = true }
    }

    @Test
    fun previewUsage() = verifyBehaviour {
        CameraRequirePermissionButtonPreview { hasLaunchedPermission = true }
    }

    private fun verifyBehaviour(content: @Composable () -> Unit = {}) = runTest {
        composeTestRule.setContent(content)

        composeTestRule.onNodeWithTag("permissionRequiredButton").performClick()
        assertTrue(hasLaunchedPermission)
    }
}
