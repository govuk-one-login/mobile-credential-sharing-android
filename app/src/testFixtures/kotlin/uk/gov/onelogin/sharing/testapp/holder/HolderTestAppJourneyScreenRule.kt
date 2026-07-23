package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesScreenRule

class HolderTestAppJourneyScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private var hasClosedJourney: Boolean = false

    private val prerequisitesScreenRule = HolderPrerequisitesScreenRule(this)

    fun assertCloseJourneyButtonDoesNotExist() = onCloseJourneyButton()
        .assertDoesNotExist()

    fun assertHasClosedJourney() = waitUntil { hasClosedJourney }

    fun assertPrerequisitesProgressIndicatorIsDisplayed() =
        prerequisitesScreenRule.assertProgressIndicatorIsDisplayed()

    fun onCloseJourneyButton(): SemanticsNodeInteraction = onNodeWithContentDescription(
        "Close",
        useUnmergedTree = true
    )

    fun performCloseJourneyClick() = onCloseJourneyButton().performClick()

    fun updateHasClosedJourney(hasClosedJourney: Boolean = true) {
        this.hasClosedJourney = hasClosedJourney
    }
}
