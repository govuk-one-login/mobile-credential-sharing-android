package uk.gov.onelogin.sharing.orchestration.scan

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(AndroidJUnit4::class)
class CredentialScannerTest {

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    private val orchestrator = FakeOrchestrator()

    @Test
    fun cameraViewfinderIsDisplayed() {
        composeTestRule.setContent {
            CredentialScanner(orchestrator = orchestrator)
        }

        composeTestRule
            .onNodeWithTag("cameraViewfinder")
            .assertExists()
            .assertIsDisplayed()
    }
}
