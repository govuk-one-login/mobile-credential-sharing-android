package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.core.presentation.permissions.MultiplePermissionsScreen

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
        FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    Manifest.permission.CAMERA,
                    permissionStatus
                )
            ),
            onLaunchPermission = { hasLaunchedPermission = true }
        )
    }

    @Test
    fun permissionDeniedBehaviour() {
        permissionStatus = PermissionStatus.Denied(false)

        composeTestRule.setContent {
            MultiplePermissionsScreen(
                state = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyRequestedPermission = false
            )
        }

        composeTestRule.assertPermissionDeniedButtonIsDisplayed()

        composeTestRule.assertPermissionDeniedTextIsDisplayed()
    }

    @Test
    fun permissionRationaleBehaviour() {
        permissionStatus = PermissionStatus.Denied(true)

        composeTestRule.setContent {
            MultiplePermissionsScreen(
                state = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyRequestedPermission = false
            )
        }

        composeTestRule.assertPermissionRationaleButtonIsDisplayed()

        composeTestRule.assertPermissionDeniedTextIsDisplayed()
    }

    @Test
    fun permissionPermanentlyDeniedBehaviour() {
        permissionStatus = PermissionStatus.Denied(false)

        composeTestRule.setContent {
            MultiplePermissionsScreen(
                state = state,
                logic = verifierScannerPermissionLogic(LocalContext.current),
                hasPreviouslyRequestedPermission = true
            )
        }

        composeTestRule.assertOpenAppSettingsButtonIsDisplayed()

        composeTestRule.assertPermissionPermanentlyDeniedButtonIsDisplayed()
    }
}
