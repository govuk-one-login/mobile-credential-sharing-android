package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import uk.gov.onelogin.sharing.holder.prerequisites.HolderPrerequisitesScreenRule

class HolderTestAppJourneyScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    private val prerequisitesScreenRule = HolderPrerequisitesScreenRule(this)

    fun assertPrerequisitesProgressIndicatorIsDisplayed() =
        prerequisitesScreenRule.assertProgressIndicatorIsDisplayed()
}
