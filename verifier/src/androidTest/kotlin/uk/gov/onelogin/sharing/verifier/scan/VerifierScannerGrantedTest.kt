package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult
import uk.gov.onelogin.orchestration.Orchestrator.Verifier.Companion.requiredPermissions
import uk.gov.onelogin.sharing.core.PermissionListExtensions.toGrantPermissionsRule

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifierScannerGrantedTest {

    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = requiredPermissions.toGrantPermissionsRule()

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule()
    )

    @Test
    fun permissionGrantedTextIsShown() = runTest {
        composeTestRule.run {
            setContent {
                VerifierScannerContent(
                    lifecycleOwner = LocalLifecycleOwner.current,
                    onUpdatePreviouslyDeniedPermission = {},
                    hasPreviouslyDeniedPermission = false,
                    permissionState = rememberMultiplePermissionsState(
                        requiredPermissions
                    ),
                    barcodeScanResultCallback = { _, _ -> }
                )
            }

            assertCameraViewfinderIsDisplayed()
        }
    }

    @Test
    fun permissionGrantedTextRenderedWithPermissionState() = runTest {
        composeTestRule.run {
            setContent {
                VerifierScannerContent(
                    lifecycleOwner = LocalLifecycleOwner.current,
                    hasPreviouslyDeniedPermission = false,
                    onUpdatePreviouslyDeniedPermission = {},
                    permissionState = rememberMultiplePermissionsState(
                        requiredPermissions
                    ),
                    barcodeScanResultCallback = BarcodeScanResult.Callback { _, _ -> }
                )
            }
            assertCameraViewfinderIsDisplayed()
        }
    }
}
