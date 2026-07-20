package uk.gov.onelogin.sharing.holder.cancellation

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import org.hamcrest.Matcher

class HolderCancellationScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
) : ComposeContentTestRule by composeTestRule {
    private var cancelJourneyClickCount: Int = 0

    fun assertCancelJourneyClickCount(matcher: Matcher<in Int>) = waitUntil {
        matcher.matches(cancelJourneyClickCount)
    }

    fun assertCancelJourneyButtonIsDisplayed(): SemanticsNodeInteraction = onCancelJourneyButton()
        .assertIsDisplayed()

    fun assertTitleIsDisplayed(): SemanticsNodeInteraction = onTitleNode()
        .assertIsDisplayed()

    fun incrementCancelJourneyClickCount() {
        cancelJourneyClickCount += 1
    }

    fun onCancelJourneyButton(): SemanticsNodeInteraction = onNodeWithText(
        "End journey",
        useUnmergedTree = true
    ).assertExists()
        .onParent()

    fun onTitleNode(): SemanticsNodeInteraction = onNodeWithText(
        "The holder journey is now cancelled"
    ).assertExists()

    fun performCancelJourneyClick() = onCancelJourneyButton().performClick()
}
