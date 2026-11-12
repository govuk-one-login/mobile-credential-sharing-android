package uk.gov.onelogin.sharing.holder.consent

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.theme.minimumTouchTarget
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

    fun assertValidAgreeButton(
        attributeCount: Int,
        useUnmergedTree: Boolean = true
    ) = onAgreeButton(attributeCount, useUnmergedTree)
        .assertExists()
        .assertHasClickAction()
        .assertIsEnabled()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertAgreeButtonIsEnabled(
        attributeCount: Int,
        useUnmergedTree: Boolean = true
    ) = onAgreeButton(
        attributeCount = attributeCount,
        useUnmergedTree = useUnmergedTree
    ).assertIsEnabled()
    fun assertAgreeButtonIsNotEnabled(
        attributeCount: Int,
        useUnmergedTree: Boolean = true
    ) = onAgreeButton(
        attributeCount = attributeCount,
        useUnmergedTree = useUnmergedTree
    ).assertIsNotEnabled()

    fun assertCancelButtonIsDisplayed() = onCancelButton().assertIsDisplayed()
    fun assertCancelButtonIsEnabled() = onCancelButton().assertIsEnabled()
    fun assertCancelButtonIsNotEnabled() = onCancelButton().assertIsNotEnabled()

    fun assertElementsDisplayed(text: String) {
        val nodes = onAllNodesWithText(text, substring = true).fetchSemanticsNodes()
        assert(nodes.isNotEmpty()) { "No nodes found containing '$text'" }
    }

    fun clickAgreeButton(
        attributeCount: Int,
        useUnmergedTree: Boolean = true
    ) = onAgreeButton(
        attributeCount = attributeCount,
        useUnmergedTree = useUnmergedTree
    ).performClick()

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

    private fun onAgreeButton(
        attributeCount: Int,
        useUnmergedTree: Boolean = true
    ): SemanticsNodeInteraction {
        onContainer().performScrollToNode(
            hasText(
                resources.getQuantityString(
                    R.plurals.consent_screen_agree_button,
                    attributeCount,
                    attributeCount
                ),
                substring = true
            )
        )

        return onAllNodesWithTag("confirmConsent", useUnmergedTree = useUnmergedTree)[0]
    }

    fun onContainer() = onNode(hasScrollAction())

    private fun onCancelButton(): SemanticsNodeInteraction {
        onContainer().performScrollToNode(
            hasText(
                resources.getString(R.string.holder_consent_screen_cancel_button)
            )
        )

        return onAllNodesWithText(
            resources.getString(R.string.holder_consent_screen_cancel_button)
        )[0]
    }
}
