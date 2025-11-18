package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule

@RunWith(AndroidJUnit4::class)
class MainActivityVerifierTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    @get:Rule
    val composeTestRule = MainActivityRule(createAndroidComposeRule<MainActivity>())

    private val verifierScannerRule = VerifierScannerRule(composeTestRule)

    @Test
    fun displaysQrScanner() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            performMenuItemClick("QR Scanner")

            verifierScannerRule.assertPermissionDeniedButtonIsDisplayed()
        }
    }
}
