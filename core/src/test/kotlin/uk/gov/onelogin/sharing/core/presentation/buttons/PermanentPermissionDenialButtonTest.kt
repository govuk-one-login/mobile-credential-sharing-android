package uk.gov.onelogin.sharing.core.presentation.buttons

import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasFlags
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermanentPermissionDenialButtonTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    val previewTitleText = "The camera permission is permanently denied."
    val previewButtonText = "Open app permissions"

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun standardUsage() = runTest {
        composeTestRule.setContent {
            PermanentPermissionDenialButton(
                context = LocalContext.current,
                titleText = previewTitleText,
                buttonText = previewButtonText
            )
        }

        composeTestRule.onNodeWithText(previewButtonText).performClick().also {
            intended(
                allOf(
                    hasAction("android.settings.APPLICATION_DETAILS_SETTINGS"),
                    hasData("package:uk.gov.onelogin.sharing.core.test"),
                    hasFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            )
        }
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            PermanentPermissionDenialButtonPreview()
        }

        composeTestRule.onNodeWithText("The camera permission is permanently denied.")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Open app permissions")
            .assertIsDisplayed()
    }
}
