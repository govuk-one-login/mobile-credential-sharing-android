package uk.gov.onelogin.sharing.verifier.verify

import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule

class VerifyCredentialRule(
    private val composeContentTestRule: ComposeContentTestRule,
): ComposeContentTestRule by composeContentTestRule {
    fun assertScannerIsDisplayed() = VerifierScannerRule(
        composeContentTestRule
    ).assertPermissionGrantedTextIsDisplayed()

    fun assertBluetoothPromptIsDisplayed() {
        Intents.intended(IntentMatchers.hasAction(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        Intents.assertNoUnverifiedIntents()
    }
}