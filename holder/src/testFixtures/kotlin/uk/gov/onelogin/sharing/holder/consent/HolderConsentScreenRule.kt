package uk.gov.onelogin.sharing.holder.consent

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
}
