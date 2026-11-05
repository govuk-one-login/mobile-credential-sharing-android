package uk.gov.onelogin.sharing.holder.consent

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class HolderConsentScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertTitleIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_consent_screen_title)
    ).assertIsDisplayed()

    fun assertWhatYouShareHeadingIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_consent_screen_what_you_share_heading)
    ).assertIsDisplayed()

    fun assertAgreeButtonIsEnabled() = onAgreeButton().assertIsEnabled()
    fun assertAgreeButtonIsNotEnabled() = onAgreeButton().assertIsNotEnabled()

    fun assertCancelButtonIsDisplayed() = onCancelButton().assertIsDisplayed()
    fun assertCancelButtonIsEnabled() = onCancelButton().assertIsEnabled()
    fun assertCancelButtonIsNotEnabled() = onCancelButton().assertIsNotEnabled()

    fun assertElementsDisplayed(text: String) {
        val nodes = onAllNodesWithText(text, substring = true).fetchSemanticsNodes()
        assert(nodes.isNotEmpty()) { "No nodes found containing '$text'" }
    }

    fun clickAgreeButton() = onAgreeButton().performClick()
    fun clickCancelButton() = onCancelButton().performClick()

    fun assertPrivacyNoticeLinkIsDisplayed(organizationName: String) = onNodeWithText(
        resources.getString(
            R.string.holder_consent_screen_privacy_notice_link,
            organizationName
        )
    ).assertIsDisplayed()

    fun clickPrivacyNoticeLink(organizationName: String) = onNodeWithText(
        resources.getString(
            R.string.holder_consent_screen_privacy_notice_link,
            organizationName
        )
    ).performClick()

    private fun onAgreeButton(): SemanticsNodeInteraction = onAllNodesWithText(
        "Agree and share",
        substring = true
    )[0]

    private fun onCancelButton(): SemanticsNodeInteraction = onNodeWithText(
        resources.getString(R.string.holder_consent_screen_cancel_button)
    )
}
