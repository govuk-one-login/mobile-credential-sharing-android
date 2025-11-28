package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRule
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrScreenRule
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
class MainActivityVerifierTest {
    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant()

    @get:Rule
    val composeTestRule = MainActivityRule(createAndroidComposeRule<MainActivity>())

    private val verifierScannerRule = VerifierScannerRule(composeTestRule)
    private val scannedInvalidQrRule = ScannedInvalidQrScreenRule(composeTestRule)
    private val connectWithHolderRule = ConnectWithHolderDeviceRule(composeTestRule)

    @Test
    fun displaysConnectWithHolderDevice() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            performMenuItemClick("Connect with credential holder")
        }
        connectWithHolderRule.assertBasicInformationIsDisplayed(validBarcodeDataResult.data)
    }

    @Test
    fun displaysInvalidQrError() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            performMenuItemClick("Error: Scanned invalid barcode")
        }
        scannedInvalidQrRule.assertTitleIsDisplayed()
    }

    @Test
    fun displaysQrScanner() = runTest {
        composeTestRule.run {
            performVerifierTabClick()
            performMenuItemClick("QR Scanner")
        }
        verifierScannerRule.assertPermissionDeniedButtonIsDisplayed()
    }
}
