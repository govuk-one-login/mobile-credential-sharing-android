package uk.gov.onelogin.sharing.testapp

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.R as verifierR

@RunWith(AndroidJUnit4::class)
class MainActivityVerifierTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.CAMERA
    )

    @get:Rule
    val composeTestRule = MainActivityRule(createAndroidComposeRule<MainActivity>())

    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources

    @Test
    fun displaysWelcomeTextAndQrCode() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            performMenuItemClick("QR Scanner")

            onNodeWithText(
                resources.getString(verifierR.string.camera_permission_is_enabled)
            ).assertExists()
                .assertIsDisplayed()
        }
    }
}
