package uk.gov.onelogin.sharing.verifier.scan

import android.content.res.Resources
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import uk.gov.onelogin.sharing.verifier.R

class VerifierScannerRule(
    composeTestRule: ComposeContentTestRule,
    private val permissionDeniedText: String,
    private val permissionGrantedText: String
) : ComposeContentTestRule by composeTestRule {

    constructor(
        resources: Resources,
        composeTestRule: ComposeContentTestRule
    ) : this(
        composeTestRule = composeTestRule,
        permissionDeniedText = resources.getString(R.string.enable_camera_permission_to_continue),
        permissionGrantedText = resources.getString(R.string.camera_permission_is_enabled)
    )

    fun assertPermissionDeniedButtonIsDisplayed() = onPermissionDeniedButton().assertIsDisplayed()

    fun assertPermissionGrantedTextIsDisplayed() = onPermissionGrantedText().assertIsDisplayed()

    fun onPermissionDeniedButton() = onNodeWithText(permissionDeniedText).assertExists()

    fun onPermissionGrantedText() = onNodeWithText(permissionGrantedText).assertExists()

    fun performPermissionDeniedClick() = onPermissionDeniedButton().performClick()

    fun render(modifier: Modifier = Modifier) {
        setContent {
            VerifierScanner(
                modifier = modifier
            )
        }
    }
}
