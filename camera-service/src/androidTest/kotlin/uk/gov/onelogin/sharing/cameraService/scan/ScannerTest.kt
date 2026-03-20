package uk.gov.onelogin.sharing.cameraService.scan

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import gov.onelogin.sharing.cameraservice.scan.FakeScanController
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.cameraService.state.CompleteVerifierScannerState

@RunWith(AndroidJUnit4::class)
class ScannerTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = ScannerViewModel(
        state = CompleteVerifierScannerState(),
        observer = FakeScanController()
    )

    @Test
    fun cameraViewfinderIsDisplayed() {
        composeTestRule.setContent {
            Scanner(viewModel = viewModel)
        }

        composeTestRule
            .onNodeWithTag("cameraViewfinder")
            .assertExists()
            .assertIsDisplayed()
    }
}
