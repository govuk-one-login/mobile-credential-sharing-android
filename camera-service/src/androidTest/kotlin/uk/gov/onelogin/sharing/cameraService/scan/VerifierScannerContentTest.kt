package uk.gov.onelogin.sharing.cameraService.scan

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VerifierScannerContentTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cameraViewfinderIsDisplayedWhenPermissionGranted() = runTest {
        composeTestRule.setContent {
            VerifierScannerContent(
                lifecycleOwner = LocalLifecycleOwner.current,
                barcodeScanResultCallback = { _, _ -> }
            )
        }

        composeTestRule
            .onNodeWithTag("cameraViewfinder")
            .assertExists()
            .assertIsDisplayed()
    }
}
