package uk.gov.onelogin.sharing.verifier.scan.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.PermissionStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.permission.PermissionScreen
import uk.gov.onelogin.sharing.verifier.scan.verifierScannerPermissionLogic

@RunWith(AndroidJUnit4::class)
class CameraPermissionRationaleButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasLaunchedPermission = false

    @Test
    fun standardUsage() = verifyBehaviour {
        CameraPermissionRationaleButton { hasLaunchedPermission = true }
    }

    @Test
    fun previewUsage() = verifyBehaviour {
        CameraPermissionRationaleButtonPreview { hasLaunchedPermission = true }
    }

    private fun verifyBehaviour(content: @Composable () -> Unit = {}) = runTest {
        composeTestRule.setContent(content)

        composeTestRule.onNodeWithTag("permissionRationaleButton").performClick()
        assertTrue(hasLaunchedPermission)
    }
}
