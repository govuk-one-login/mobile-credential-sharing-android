package uk.gov.onelogin.sharing.core.presentation.bluetooth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BluetoothConnectionErrorScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun titleIsDisplayed() {
        composeTestRule.setContent {
            BluetoothConnectionErrorScreen(title = "Bluetooth connection failed")
        }

        composeTestRule.onNodeWithText("Bluetooth connection failed").assertIsDisplayed()
    }

    @Test
    fun tryAgainButtonIsDisplayed() {
        composeTestRule.setContent {
            BluetoothConnectionErrorScreen(title = "Bluetooth connection failed")
        }

        composeTestRule.onNodeWithText("Try again").assertIsDisplayed()
    }

    @Test
    fun tryAgainButtonInvokesTryAgainCallback() {
        var clicked = false
        composeTestRule.setContent {
            BluetoothConnectionErrorScreen(
                title = "Bluetooth connection failed",
                onTryAgainClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Try again").performClick()

        assertTrue(clicked)
    }
}
