package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationale
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class HolderPermissionGateContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `calls onAllGranted when bluetooth permissions are granted`() {
        var granted = false

        composeTestRule.setContent {
            HolderPermissionGateContent(
                permissionsState = bluetoothPermissionsGranted,
                hasPreviouslyRequested = false,
                onGrantAll = { granted = true }
            )
        }

        composeTestRule.waitUntil { granted }
    }

    @Test
    fun `shows enable prompt when not previously requested`() {
        composeTestRule.setContent {
            HolderPermissionGateContent(
                permissionsState = bluetoothPermissionsDenied,
                hasPreviouslyRequested = false,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Please enable bluetooth permissions to continue")
            .assertIsDisplayed()
    }

    @Test
    fun `shows permanently denied prompt when bluetooth is denied`() {
        composeTestRule.setContent {
            HolderPermissionGateContent(
                permissionsState = bluetoothPermissionsDenied,
                hasPreviouslyRequested = true,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Bluetooth permissions have been permanently denied")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Open app permissions")
            .assertIsDisplayed()
    }

    @Test
    fun `shows rationale when bluetooth denied with rationale`() {
        composeTestRule.setContent {
            HolderPermissionGateContent(
                permissionsState = bluetoothPermissionsDeniedWithRationale,
                hasPreviouslyRequested = true,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Bluetooth permissions were denied")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Please enable bluetooth permissions to continue")
            .assertIsDisplayed()
    }
}
