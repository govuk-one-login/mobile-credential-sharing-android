package uk.gov.onelogin.sharing.bluetooth

import android.app.Activity
import android.app.Instrumentation
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
class EnableBluetoothPromptTest {
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
    fun `returns true when user accepts enable bluetooth`() {
        var callbackResult: Boolean? = null
        val okResult = Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            null
        )
        Intents.intending(
            IntentMatchers.hasAction(
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        ).respondWith(okResult)

        composeTestRule.setContent {
            EnableBluetoothPrompt { enabled ->
                callbackResult = enabled
            }
        }

        composeTestRule.waitForIdle()

        Intents.intended(
            IntentMatchers.hasAction(
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        )

        assertEquals(true, callbackResult)
    }

    @Test
    fun `returns false when user declines enable bluetooth`() {
        var callbackResult: Boolean? = null
        val canceledResult = Instrumentation.ActivityResult(
            Activity.RESULT_CANCELED,
            null
        )
        Intents.intending(
            IntentMatchers.hasAction(
                BluetoothAdapter.ACTION_REQUEST_ENABLE
            )
        ).respondWith(canceledResult)

        composeTestRule.setContent {
            EnableBluetoothPrompt { enabled ->
                callbackResult = enabled
            }
        }

        composeTestRule.waitForIdle()

        Intents.intended(IntentMatchers.hasAction(BluetoothAdapter.ACTION_REQUEST_ENABLE))

        assertEquals(false, callbackResult)
    }
}
