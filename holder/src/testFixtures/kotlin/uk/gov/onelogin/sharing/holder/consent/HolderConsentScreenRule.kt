package uk.gov.onelogin.sharing.holder.consent

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
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
        resources.getString(R.string.holder_consent_title)
    ).assertIsDisplayed()

    fun assertAcceptButtonIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_consent_accept)
    ).assertIsDisplayed()

    fun assertDenyButtonIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_consent_deny)
    ).assertIsDisplayed()

    fun assertElementsDisplayed(text: String) {
        val nodes = onAllNodesWithText(text, substring = true).fetchSemanticsNodes()
        assert(nodes.isNotEmpty()) { "No nodes found containing '$text'" }
    }

    fun clickDenyButton() = onNodeWithText(
        resources.getString(R.string.holder_consent_deny)
    ).performClick()

    fun assertDenyDialogIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_consent_deny_dialog_title)
    ).assertIsDisplayed()

    fun assertDenyDialogIsNotDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_consent_deny_dialog_title)
    ).assertIsNotDisplayed()

    fun clickDenyDialogConfirm() = onAllNodesWithText(
        resources.getString(R.string.holder_consent_deny)
    )[1].performClick()

    fun clickDenyDialogDismiss() = onNodeWithText(
        resources.getString(R.string.holder_consent_deny_dialog_dismiss)
    ).performClick()
}
