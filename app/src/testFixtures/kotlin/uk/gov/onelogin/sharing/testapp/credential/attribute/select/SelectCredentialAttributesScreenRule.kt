package uk.gov.onelogin.sharing.testapp.credential.attribute.select

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyChild
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.orchestration.verificationrequest.AttributeGroup
import uk.gov.onelogin.sharing.testapp.ATTRIBUTE_GROUP_ITEM_TAG
import uk.gov.onelogin.sharing.testapp.R

class SelectCredentialAttributesScreenRule(
    composeTestRule: ComposeContentTestRule,
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>()
        .resources
) : ComposeContentTestRule by composeTestRule {
    private var confirmedAttributeGroup: AttributeGroup? = null

    fun assertConfirmedAttributeGroupEquals(group: AttributeGroup) = waitUntil {
        group == confirmedAttributeGroup
    }

    fun assertOptionIsSelected(option: VerifierAttributeOption) = onNode(
        hasParent(
            hasTestTag("attribute_group_menu")
        ) and hasTestTag("dropdown_text")
    ).assertTextContains(option.displayName)

    fun assertOptionIsSelected(option: ReaderAuthOption) = onNode(
        hasParent(
            hasTestTag("reader_auth_menu")
        ) and hasTestTag("dropdown_text")
    ).assertTextContains(option.displayName)

    fun onVerifierOptionText(option: VerifierAttributeOption) = onNode(
        hasTestTag(
            ATTRIBUTE_GROUP_ITEM_TAG
        ) and hasAnyChild(hasText(option.displayName)),
        useUnmergedTree = true
    )

    fun onReaderAuthOptionText(option: ReaderAuthOption) = onNode(
        hasTestTag(
            "reader_auth_item"
        ) and hasAnyChild(hasText(option.displayName)),
        useUnmergedTree = true
    )

    fun performAttributeGroupMenuClick() = onNodeWithTag(
        "attribute_group_menu",
        useUnmergedTree = true
    ).performScrollTo().performClick()

    fun performReaderAuthMenuClick() = onNodeWithTag(
        "reader_auth_menu",
        useUnmergedTree = true
    ).performScrollTo().performClick()

    fun performAttributeGroupClick(option: VerifierAttributeOption) {
        performAttributeGroupMenuClick()
        onVerifierOptionText(option).performClick()
    }

    fun performReaderAuthClick(option: ReaderAuthOption) {
        performReaderAuthMenuClick()
        onReaderAuthOptionText(option).performClick()
    }

    fun performVerifyCredentialClick() = onNodeWithText(
        resources.getString(R.string.verify_credential),
        useUnmergedTree = true
    ).performScrollTo().performClick()

    fun updateConfirmedAttributeGroup(group: AttributeGroup) {
        this.confirmedAttributeGroup = group
    }
}
