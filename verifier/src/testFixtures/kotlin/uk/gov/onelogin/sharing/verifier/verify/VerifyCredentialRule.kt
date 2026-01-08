package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPromptRule
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule

class VerifyCredentialRule(composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {
    fun assertBluetoothPromptIsDisplayed() = EnableBluetoothPromptRule(
        this
    ).assertIsDisplayed()
}
