package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.permission.PermissionScreen
import uk.gov.onelogin.sharing.verifier.R

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierScannerPermissionLogicTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule()
    )

    private var permissionStatus: PermissionStatus = PermissionStatus.Granted

    private var hasLaunchedPermission = false
    private val state by lazy {
        FakePermissionState(
            permission = Manifest.permission.CAMERA,
            status = permissionStatus,
            onLaunchPermission = { hasLaunchedPermission = true }
        )
    }

    @Test
    fun permissionGrantedBehaviour() {
        composeTestRule.setContent {
            PermissionScreen(
                permissionState = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyDeniedPermission = false
            )
        }

        composeTestRule.assertPermissionGrantedTextIsDisplayed()
    }

    @Test
    fun permissionDeniedBehaviour() {
        permissionStatus = PermissionStatus.Denied(false)

        composeTestRule.setContent {
            PermissionScreen(
                permissionState = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyDeniedPermission = false
            )
        }

        composeTestRule.assertPermissionDeniedButtonIsDisplayed()
    }

    @Test
    fun permissionRationaleBehaviour() {
        permissionStatus = PermissionStatus.Denied(true)

        composeTestRule.setContent {
            PermissionScreen(
                permissionState = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyDeniedPermission = false
            )
        }

        composeTestRule.assertPermissionDeniedButtonIsDisplayed()
    }

    @Test
    fun permissionPermanentlyDeniedBehaviour() {
        permissionStatus = PermissionStatus.Denied(false)

        composeTestRule.setContent {
            PermissionScreen(
                permissionState = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyDeniedPermission = true
            )
        }

        composeTestRule.assertOpenAppSettingsButtonIsDisplayed()

        composeTestRule.onNodeWithText(
            resources.getString(R.string.open_app_permissions)
        ).assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun verifyPreview() {
        composeTestRule.setContent {
            VerifierScannerPreview(
                state to true
            )
        }

        composeTestRule.assertPermissionGrantedTextIsDisplayed()
    }
}
