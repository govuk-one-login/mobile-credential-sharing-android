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
import uk.gov.android.ui.componentsv2.permission.PermissionScreen

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierScannerPermissionGrantedTest {

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

        composeTestRule.assertCameraViewfinderIsDisplayed()
    }

    @Test
    fun verifyPreview() {
        composeTestRule.setContent {
            VerifierScannerPreview(
                state to true
            )
        }

        composeTestRule.assertCameraViewfinderIsDisplayed()
    }
}
