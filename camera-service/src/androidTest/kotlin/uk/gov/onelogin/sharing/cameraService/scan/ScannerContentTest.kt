package uk.gov.onelogin.sharing.cameraService.scan

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScannerContentTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cameraViewfinderIsDisplayed() {
        composeTestRule.setContent {
            ScannerContent(
                lifecycleOwner = LocalLifecycleOwner.current,
                barcodeScanResultCallback = { _, _ -> }
            )
        }

        composeTestRule
            .onNodeWithTag("cameraViewfinder")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun scannerContentPreviewIsDisplayed() {
        composeTestRule.setContent {
            ScannerContentPreview()
        }

        composeTestRule
            .onNodeWithTag("preview")
            .assertExists()
            .assertIsDisplayed()
    }
}
