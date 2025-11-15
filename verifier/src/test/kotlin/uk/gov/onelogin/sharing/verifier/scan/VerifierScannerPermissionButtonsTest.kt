package uk.gov.onelogin.sharing.verifier.scan

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerPermissionButtons.CameraPermissionRationaleButton
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerPermissionButtons.CameraRequirePermissionButton
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerPermissionButtons.PermanentCameraDenial

@RunWith(AndroidJUnit4::class)
class VerifierScannerPermissionButtonsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasLaunchedPermission = false

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun permissionDeniedButton() {
        composeTestRule.setContent {
            CameraRequirePermissionButton {
                hasLaunchedPermission = true
            }
        }

        composeTestRule.onNodeWithTag("permissionRequiredButton").performClick()
        assertTrue(hasLaunchedPermission)
    }

    @Test
    fun permissionRationaleButton() {
        composeTestRule.setContent {
            CameraPermissionRationaleButton {
                hasLaunchedPermission = true
            }
        }

        composeTestRule.onNodeWithTag("permissionRationaleButton").performClick()
        assertTrue(hasLaunchedPermission)
    }

    @Test
    fun permissionPermanentlyDeniedButton() {
        composeTestRule.setContent {
            PermanentCameraDenial(
                context = LocalContext.current
            )
        }

        composeTestRule.onNodeWithTag("permissionPermanentDenialButton").performClick()

        intended(
            allOf(
                hasAction("android.settings.APPLICATION_DETAILS_SETTINGS"),
                hasData("package:uk.gov.onelogin.sharing.verifier.test"),
                hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        )
    }
}
