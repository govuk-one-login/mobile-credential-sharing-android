package uk.gov.onelogin.sharing.holder.consent

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.theme.minimumTouchTarget
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentAttribute
import uk.gov.onelogin.sharing.orchestration.holder.session.ConsentPresentation

class HolderConsentScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertTitleIsDisplayed(useUnmergedTree: Boolean = false) = onTitle(useUnmergedTree)
        .assertIsDisplayed()

    fun assertWhatYouShareHeadingIsDisplayed(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_consent_screen_what_you_share_heading),
        useUnmergedTree = useUnmergedTree
    ).assertExists()
        .assert(isHeading())
        .assertIsDisplayed()

    fun assertValidAgreeButton(
        attributeCount: Int,
        useUnmergedTree: Boolean = false
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
        useUnmergedTree: Boolean = false
    ) = onAgreeButton(
        attributeCount = attributeCount,
        useUnmergedTree = useUnmergedTree
    ).assertIsNotEnabled()

    fun assertCancelButtonIsDisplayed() = onCancelButton().assertIsDisplayed()
    fun assertCancelButtonIsEnabled() = onCancelButton().assertIsEnabled()
    fun assertCancelButtonIsNotEnabled() = onCancelButton().assertIsNotEnabled()

    fun assertDetailCountFromDocumentIsDisplayed(
        presentation: ConsentPresentation,
        useUnmergedTree: Boolean = false
    ) = onNodeWithText(
        resources.getQuantityString(
            R.plurals.consent_card_heading,
            presentation.attributeCount,
            presentation.attributeCount,
            resources.getString(R.string.holder_consent_doc_mdl)
        ),
        useUnmergedTree = useUnmergedTree
    ).assertExists()
        .assertIsDisplayed()

    fun assertElementsDisplayed(text: String) {
        val nodes = onAllNodesWithText(text, substring = true).fetchSemanticsNodes()
        assert(nodes.isNotEmpty()) { "No nodes found containing '$text'" }
    }

    fun assertElementsPartiallyRetained(
        organizationName: String,
        useUnmergedTree: Boolean = false,
    ) {
        assertHiddenTextIsDisplayable(
            resources.getString(
                R.string.consent_card_will_keep,
                organizationName
            ),
            useUnmergedTree
        )
        assertHiddenTextIsDisplayable(
            resources.getString(
                R.string.consent_card_will_not_keep,
                organizationName
            ),
            useUnmergedTree
        )
    }

    private fun assertHiddenTextIsDisplayable(
        textToScrollTo: String,
        useUnmergedTree: Boolean
    ) {
        onContainer().performScrollToNode(
            hasText(textToScrollTo),
        )
        onNodeWithText(
            textToScrollTo,
            useUnmergedTree = useUnmergedTree
        ).assertExists()
            .assertIsDisplayed()
    }

    fun assertValidAttributes(
        attributes: List<ConsentAttribute>,
        useUnmergedTree: Boolean = false
    ) {
        attributes.let(ConsentAttributeResolver::toDisplayAttributes)
            .map { retainedAttribute ->
                when (val label = retainedAttribute.label) {
                    is DisplayAttribute.Label.Resource -> {
                        resources.getString(
                            label.res,
                            label.arg
                        )
                    }

                    is DisplayAttribute.Label.Raw -> {
                        label.text
                    }
                }
            }.map { onNodeWithText(it, useUnmergedTree = useUnmergedTree) }
            .forEach { it.assertExists() }
    }

    fun assertValidBody(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onBody(organizationName, useUnmergedTree)
        .assertExists()

    fun assertValidCancelButton(
        useUnmergedTree: Boolean = true
    ) = onCancelButton(useUnmergedTree)
        .assertExists()
        .assertHasClickAction()
        .assertIsEnabled()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertNoElementsRetained(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.consent_card_none_kept),
        useUnmergedTree = useUnmergedTree
    ).assertExists()
        .assertIsDisplayed()

    fun assertValidPrivacyNoticeLink(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onPrivacyNoticeLink(
        organizationName,
        useUnmergedTree
    ).assertExists()
        .assertHasClickAction()
        .assertIsEnabled()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidTitle(
        useUnmergedTree: Boolean = false
    ) = onTitle(useUnmergedTree)
        .assertExists()
        .assert(isHeading())
        .assertIsDisplayed()

    fun assertValidUi(
        presentation: ConsentPresentation,
        useUnmergedTree: Boolean = false
    ) {
        assertValidTitle(useUnmergedTree)
        assertValidBody(presentation.normalisedOrganizationName, useUnmergedTree)
        assertValidPrivacyNoticeLink(presentation.normalisedOrganizationName, useUnmergedTree)
        assertWhatYouShareHeadingIsDisplayed(useUnmergedTree)
        assertDetailCountFromDocumentIsDisplayed(presentation, useUnmergedTree)
        assertValidUnretainedAttributes(presentation, useUnmergedTree)
        assertValidRetainedAttributes(presentation, useUnmergedTree)
        assertValidAgreeButton(presentation.attributeCount, useUnmergedTree)
        assertValidCancelButton(useUnmergedTree)
    }

    fun assertValidUnretainedAttributes(
        presentation: ConsentPresentation,
        useUnmergedTree: Boolean
    ) = presentation.firstDocumentAttributes
        ?.filterNot(ConsentAttribute::intentToRetain)
        ?.let { assertValidAttributes(it, useUnmergedTree) }

    fun assertValidRetainedAttributes(
        presentation: ConsentPresentation,
        useUnmergedTree: Boolean = false,
    ) = presentation.firstDocumentAttributes
        ?.filter(ConsentAttribute::intentToRetain)
        ?.let { assertValidAttributes(it, useUnmergedTree) }

    fun clickAgreeButton(
        attributeCount: Int,
        useUnmergedTree: Boolean = true
    ) = onAgreeButton(
        attributeCount = attributeCount,
        useUnmergedTree = useUnmergedTree
    ).performClick()

    fun clickCancelButton() = onCancelButton().performClick()

    fun clickPrivacyNoticeLink(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onPrivacyNoticeLink(
        organizationName,
        useUnmergedTree
    ).performClick()

    private fun onAgreeButton(
        attributeCount: Int,
        useUnmergedTree: Boolean = false
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

        return onAllNodesWithText(
            resources.getQuantityString(
                R.plurals.consent_screen_agree_button,
                attributeCount,
                attributeCount
            ),
            substring = true,
            useUnmergedTree = useUnmergedTree
        ).onFirst()
    }

    fun onBody(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ) = onNodeWithText(
        resources.getString(
            R.string.holder_consent_organization_sentence,
            organizationName
        ),
        useUnmergedTree = useUnmergedTree
    )

    fun onContainer() = onNode(hasScrollAction())

    private fun onCancelButton(useUnmergedTree: Boolean = false): SemanticsNodeInteraction {
        onContainer().performScrollToNode(
            hasText(
                resources.getString(R.string.holder_consent_screen_cancel_button)
            )
        )

        return onAllNodesWithText(
            resources.getString(R.string.holder_consent_screen_cancel_button),
            useUnmergedTree = useUnmergedTree
        )[0]
    }

    private fun onPrivacyNoticeLink(
        organizationName: String,
        useUnmergedTree: Boolean = false
    ): SemanticsNodeInteraction = onNodeWithText(
        resources.getString(
            R.string.holder_consent_screen_privacy_notice_link,
            organizationName
        ),
        useUnmergedTree = useUnmergedTree
    )

    fun onTitle(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_consent_screen_title),
        useUnmergedTree = useUnmergedTree
    )
}
