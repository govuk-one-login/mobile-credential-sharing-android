package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers

class EnableBluetoothPromptRule(private val composeContentTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeContentTestRule {
    fun assertIsDisplayed() {
        Intents.intended(IntentMatchers.hasAction(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        Intents.assertNoUnverifiedIntents()
    }

    fun assertIsNotDisplayed() {
        Intents.assertNoUnverifiedIntents()
    }
}
