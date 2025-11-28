package uk.gov.onelogin.sharing.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BluetoothStateManagerPromptTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun shouldReturnBluetoothOnWhenResultOK() {
        var capturedStatus: BluetoothStatus? = null

        val result = android.app.Instrumentation.ActivityResult(Activity.RESULT_OK, null)

        Intents.intending(IntentMatchers.hasAction(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            .respondWith(result)

        composeTestRule.setContent {
            BluetoothStateManagerPrompt(
                onStateChange = { status ->
                    capturedStatus = status
                }
            )
        }

        composeTestRule.waitForIdle()

        assertEquals(BluetoothStatus.BLUETOOTH_ON, capturedStatus)
    }

    @Test
    fun shouldReturnBluetoothOffWhenResultIsNotOk() {
        var capturedStatus: BluetoothStatus? = null

        val result = android.app.Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null)

        Intents.intending(IntentMatchers.hasAction(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            .respondWith(result)

        composeTestRule.setContent {
            BluetoothStateManagerPrompt(
                onStateChange = { status ->
                    capturedStatus = status
                }
            )
        }

        composeTestRule.waitForIdle()

        assertEquals(BluetoothStatus.BLUETOOTH_OFF, capturedStatus)
    }
}
