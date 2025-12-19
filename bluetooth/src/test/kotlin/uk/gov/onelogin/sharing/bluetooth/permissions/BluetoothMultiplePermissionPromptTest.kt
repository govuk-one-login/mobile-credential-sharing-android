package uk.gov.onelogin.sharing.bluetooth.permissions

import android.Manifest
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import kotlin.test.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState

@RunWith(AndroidJUnit4::class)
class BluetoothMultiplePermissionPromptTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testTag = "bluetoothWelcomeText"

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowEnableBluetoothButtonWhenShowRationaleTrue() {
        composeTestRule.setContent {
            var hasLaunched = remember { false }
            BluetoothPermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_CONNECT,
                            status = PermissionStatus.Denied(true)
                        ),
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                            status = PermissionStatus.Denied(true)
                        )
                    ),
                    onLaunchPermission = { hasLaunched = true }
                ),
                hasPreviouslyRequestedPermission = true,
                modifier = Modifier
            ) {
            }
        }

        composeTestRule.onNodeWithText("Enable bluetooth permissions").assertIsDisplayed()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowEnableBluetoothButtonWhenRequestingFirstTime() {
        composeTestRule.setContent {
            var hasLaunched = remember { false }
            BluetoothPermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_CONNECT,
                            status = PermissionStatus.Denied(false)
                        ),
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                            status = PermissionStatus.Denied(false)
                        )
                    ),
                    onLaunchPermission = { hasLaunched = false }
                ),
                hasPreviouslyRequestedPermission = false,
                modifier = Modifier
            ) {
            }
        }

        composeTestRule.onNodeWithText("Enable bluetooth permissions").assertIsDisplayed()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowOpenSettingsWhenPermissionsPermanentlyDenied() {
        composeTestRule.setContent {
            var hasLaunched = remember { false }
            BluetoothPermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_CONNECT,
                            status = PermissionStatus.Denied(false)
                        ),
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                            status = PermissionStatus.Denied(false)
                        )
                    ),
                    onLaunchPermission = { hasLaunched = true }
                ),
                hasPreviouslyRequestedPermission = true,
                modifier = Modifier
            ) {
            }
        }

        composeTestRule.onNodeWithText("Open settings").assertIsDisplayed()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldCallOnGrantedPermissionsWhenAllPermissionsAreGranted() {
        var onGrantedLambdaCalled = false
        val grantedContentTestTag = "granted-content"

        composeTestRule.setContent {
            BluetoothPermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_CONNECT,
                            status = PermissionStatus.Granted
                        ),
                        FakePermissionState(
                            permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                            status = PermissionStatus.Granted
                        )
                    ),
                    onLaunchPermission = {}
                ),
                hasPreviouslyRequestedPermission = true,
                modifier = Modifier
            ) {
                onGrantedLambdaCalled = true
                Text(
                    text = "Permissions Granted",
                    modifier = Modifier.testTag(grantedContentTestTag)
                )
            }
        }

        assertEquals(true, onGrantedLambdaCalled)
        composeTestRule.onNodeWithTag(grantedContentTestTag).assertIsDisplayed()
        composeTestRule.onNodeWithText("Permissions Granted").assertIsDisplayed()
    }
}
