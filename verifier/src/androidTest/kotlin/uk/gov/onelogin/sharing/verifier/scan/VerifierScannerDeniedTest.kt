package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.camera.qr.BarcodeScanResult

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalPermissionsApi::class)
class VerifierScannerDeniedTest {
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        resources = resources,
        composeTestRule = createComposeRule()
    )

    @Test
    fun permissionPermanentlyDeniedIsShown() = runTest {
        composeTestRule.run {
            composeTestRule.setContent {
                VerifierScannerContent(
                    lifecycleOwner = LocalLifecycleOwner.current,
                    permissionState = rememberPermissionState(Manifest.permission.CAMERA),
                    hasPreviouslyDeniedPermission = true,
                    onUpdatePreviouslyDeniedPermission = {},
                    barcodeScanResultCallback = BarcodeScanResult.Callback { _, _ -> }
                )
            }

            assertPermissionPermanentlyDeniedButtonIsDisplayed()
        }
    }
}
