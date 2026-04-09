package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.allPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.allPermissionsDeniedWithRationale
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.allPermissionsGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedCameraGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDeniedWithRationaleCameraGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.cameraPermissionDenied

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierPermissionGateContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `calls onAllGranted when all permissions are granted`() {
        var granted = false

        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = allPermissionsGranted,
                hasPreviouslyRequested = false,
                onGrantAll = { granted = true }
            )
        }

        composeTestRule.waitUntil { granted }
    }

    @Test
    fun `shows camera permanently denied prompt when camera is denied`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = cameraPermissionDenied,
                hasPreviouslyRequested = true,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("The camera permission is permanently denied.")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Open app permissions")
            .assertIsDisplayed()
    }

    @Test
    fun `shows bluetooth permanently denied prompt when bluetooth is denied`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = bluetoothPermissionsDeniedCameraGranted,
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
    fun `shows camera enable prompt when not previously requested`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = cameraPermissionDenied,
                hasPreviouslyRequested = false,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Please enable the camera permission to continue.")
            .assertIsDisplayed()
    }

    @Test
    fun `shows bluetooth rationale when bluetooth denied with rationale`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = bluetoothPermissionsDeniedWithRationaleCameraGranted,
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

    @Test
    fun `shows generic permanently denied prompt when both permissions are denied`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = allPermissionsDenied,
                hasPreviouslyRequested = true,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Camera and Bluetooth permissions have been permanently denied.")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Open app permissions")
            .assertIsDisplayed()
    }

    @Test
    fun `shows generic enable prompt when both permissions missing and not previously requested`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = allPermissionsDenied,
                hasPreviouslyRequested = false,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Please enable camera and Bluetooth permissions to continue.")
            .assertIsDisplayed()
    }

    @Test
    fun `shows generic rationale when both permissions denied with rationale`() {
        composeTestRule.setContent {
            VerifierPermissionGateContent(
                permissionsState = allPermissionsDeniedWithRationale,
                hasPreviouslyRequested = true,
                onGrantAll = {}
            )
        }

        composeTestRule
            .onNodeWithText("Camera and Bluetooth permissions were denied")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithText("Please enable camera and Bluetooth permissions to continue.")
            .assertIsDisplayed()
    }
}
