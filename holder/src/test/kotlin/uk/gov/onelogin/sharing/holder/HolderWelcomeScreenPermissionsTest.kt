package uk.gov.onelogin.sharing.holder

import android.Manifest
import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.holder.presentation.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenPermissionsTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(
        composeTestRule = createComposeRule(),
        resources = resources
    )

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should show enablePermissionsButton when permissions have not been requested yet`() {
        var hasLaunched = false
        val fakeState = FakeMultiplePermissionsState(
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
        )

        composeTestRule.setContent {
            BluetoothPermissionPrompt(
                multiplePermissionsState = fakeState,
                hasPreviouslyRequestedPermission = false
            ) {}
        }

        composeTestRule.assertEnablePermissionsButtonTextIsDisplayed()
        composeTestRule.onEnablePermissionsButtonText().performClick()
        assertTrue { hasLaunched }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should show permissionRationale when permissions have been denied once`() {
        var hasLaunched = false
        val fakeState = FakeMultiplePermissionsState(
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
        )

        composeTestRule.setContent {
            BluetoothPermissionPrompt(
                multiplePermissionsState = fakeState,
                hasPreviouslyRequestedPermission = true
            ) { }
        }

        composeTestRule.assertEnablePermissionsButtonTextIsDisplayed()
        composeTestRule.onEnablePermissionsButtonText().performClick()

        assertTrue { hasLaunched }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should show open settings button when permissions permanently denied`() {
        val fakeState = FakeMultiplePermissionsState(
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
            { }
        )

        composeTestRule.setContent {
            BluetoothPermissionPrompt(
                multiplePermissionsState = fakeState,
                hasPreviouslyRequestedPermission = true
            ) { }
        }

        composeTestRule.assertOpenAppSettingsButton()
        composeTestRule.assertPermanentlyDeniedTextIsDisplayed()
    }
}
