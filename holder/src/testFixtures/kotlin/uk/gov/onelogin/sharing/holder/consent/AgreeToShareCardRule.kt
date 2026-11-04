package uk.gov.onelogin.sharing.holder.consent

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class AgreeToShareCardRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertDocumentNameIsDisplayed() = assertTextIsDisplayed(
        resources.getString(R.string.holder_consent_doc_mdl)
    )

    fun assertAttributeCountIsDisplayed(count: Int) = assertTextIsDisplayed(count.toString())

    fun assertNoneKeptCopyIsDisplayed() = assertTextIsDisplayed(
        resources.getString(R.string.holder_consent_card_none_kept)
    )

    fun assertAllKeptCopyIsDisplayed(organizationName: String) = assertTextIsDisplayed(
        resources.getString(R.string.holder_consent_card_all_kept, organizationName)
    )

    fun assertWillKeepCopyIsDisplayed(organizationName: String) = assertTextIsDisplayed(
        resources.getString(R.string.holder_consent_card_will_keep, organizationName)
    )

    fun assertWillNotKeepCopyIsDisplayed(organizationName: String) = assertTextIsDisplayed(
        resources.getString(R.string.holder_consent_card_will_not_keep, organizationName)
    )

    fun assertAttributeLabelIsDisplayed(resId: Int, vararg formatArgs: Any) = assertTextIsDisplayed(
        resources.getString(resId, *formatArgs)
    )

    private fun assertTextIsDisplayed(text: String) =
        onNodeWithText(text, substring = true).assertIsDisplayed()
}
