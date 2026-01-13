package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import uk.gov.onelogin.sharing.bluetooth.EnableBluetoothPromptRule

class VerifyCredentialRule(composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {
    fun assertBluetoothPromptIsDisplayed() = EnableBluetoothPromptRule(
        this
    ).assertIsDisplayed()
}
