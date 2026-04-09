package uk.gov.onelogin.sharing.holder.prerequisites

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class HolderPrerequisitesScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    var hasHandledPreflight: Boolean = false
        private set

    var hasPresentedEngagement: Boolean = false
        private set

    var hasUnrecoverableError: Boolean = false
        private set

    fun assertNotStartedTextIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_prerequisites_not_started)
    ).assertIsDisplayed()
    fun assertPreflightTextIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_prerequisites_preflight)
    ).assertIsDisplayed()
    fun assertReadyToPresentTextIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_prerequisites_ready_to_present)
    ).assertIsDisplayed()
    fun assertPresentingEngagementTextIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_prerequisites_presenting_engagement)
    ).assertIsDisplayed()

    fun updateHasHandledPreflight(hasHandledPreflight: Boolean = true) {
        this.hasHandledPreflight = hasHandledPreflight
    }

    fun updateHasPresentedEngagement(hasPresentedEngagement: Boolean = true) {
        this.hasPresentedEngagement = hasPresentedEngagement
    }

    fun updateHasUnrecoverableError(hasUnrecoverableError: Boolean = true) {
        this.hasUnrecoverableError = hasUnrecoverableError
    }
}
