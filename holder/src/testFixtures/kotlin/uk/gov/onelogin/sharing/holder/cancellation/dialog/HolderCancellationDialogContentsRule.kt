package uk.gov.onelogin.sharing.holder.cancellation.dialog

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

/**
 * Test rule exposing the "stop the check" dialogue's nodes and a dismissal counter.
 */
class HolderCancellationDialogContentsRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    var dismissCount: Int = 0
        private set

    fun incrementDismissCount() {
        dismissCount += 1
    }

    fun onTitle(): SemanticsNodeInteraction = onNodeWithText(
        resources.getString(R.string.holder_stop_check_dialog_title)
    )

    fun onBody(): SemanticsNodeInteraction = onNodeWithText(
        resources.getString(R.string.holder_stop_check_dialog_body)
    )

    fun onStopTheCheckButton(): SemanticsNodeInteraction = onNodeWithText(
        resources.getString(R.string.holder_stop_check_dialog_stop_button)
    )

    fun onContinueWithTheCheckButton(): SemanticsNodeInteraction = onNodeWithText(
        resources.getString(R.string.holder_stop_check_dialog_continue_button)
    )

    fun assertContentsAreDisplayed() {
        onTitle().assertIsDisplayed()
        onBody().assertIsDisplayed()
        onStopTheCheckButton().assertIsDisplayed()
        onContinueWithTheCheckButton().assertIsDisplayed()
    }

    fun performStopTheCheckClick() = onStopTheCheckButton().performClick()

    fun performContinueWithTheCheckClick() = onContinueWithTheCheckButton().performClick()
}
