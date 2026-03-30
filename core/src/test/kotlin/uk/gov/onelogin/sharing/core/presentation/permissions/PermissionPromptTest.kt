package uk.gov.onelogin.sharing.core.presentation.permissions

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

@RunWith(AndroidJUnit4::class)
class PermissionPromptTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val permanentlyDeniedText = "Permission permanently denied"
    private val enablePermissionText = "Enable permission"
    private val openSettingsText = "Open settings"
    private val deniedText = "Permission was denied"

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowEnablePermissionButtonWhenShowRationaleTrue() {
        composeTestRule.setContent {
            PermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.CAMERA,
                            status = PermissionStatus.Denied(true)
                        )
                    ),
                    onLaunchPermission = {}
                ),
                hasPreviouslyRequestedPermission = true,
                permanentlyDeniedText = permanentlyDeniedText,
                enablePermissionText = enablePermissionText,
                openSettingsText = openSettingsText,
                deniedText = deniedText
            ) {}
        }

        composeTestRule.onNodeWithText(enablePermissionText).assertIsDisplayed()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowEnablePermissionButtonWhenRequestingFirstTime() {
        composeTestRule.setContent {
            PermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.CAMERA,
                            status = PermissionStatus.Denied(false)
                        )
                    ),
                    onLaunchPermission = {}
                ),
                hasPreviouslyRequestedPermission = false,
                permanentlyDeniedText = permanentlyDeniedText,
                enablePermissionText = enablePermissionText,
                openSettingsText = openSettingsText,
                deniedText = deniedText
            ) {}
        }

        composeTestRule.onNodeWithText(enablePermissionText).assertIsDisplayed()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldShowOpenSettingsWhenPermissionsPermanentlyDenied() {
        composeTestRule.setContent {
            PermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.CAMERA,
                            status = PermissionStatus.Denied(false)
                        )
                    ),
                    onLaunchPermission = {}
                ),
                hasPreviouslyRequestedPermission = true,
                permanentlyDeniedText = permanentlyDeniedText,
                enablePermissionText = enablePermissionText,
                openSettingsText = openSettingsText,
                deniedText = deniedText
            ) {}
        }

        composeTestRule.onNodeWithText(openSettingsText).assertIsDisplayed()
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun shouldCallOnGrantedPermissionsWhenAllPermissionsAreGranted() {
        var onGrantedLambdaCalled = false
        val grantedContentTestTag = "granted-content"

        composeTestRule.setContent {
            PermissionPrompt(
                multiplePermissionsState = FakeMultiplePermissionsState(
                    permissions = listOf(
                        FakePermissionState(
                            permission = Manifest.permission.CAMERA,
                            status = PermissionStatus.Granted
                        )
                    ),
                    onLaunchPermission = {}
                ),
                hasPreviouslyRequestedPermission = true,
                permanentlyDeniedText = permanentlyDeniedText,
                enablePermissionText = enablePermissionText,
                openSettingsText = openSettingsText,
                deniedText = deniedText
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
