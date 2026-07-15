package uk.gov.onelogin.sharing.holder.awaitingresolution

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class AwaitingVerifierResolutionScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertTitleIsDisplayed() = onNodeWithText(
        resources.getString(R.string.holder_awaiting_resolution_title)
    ).assertIsDisplayed()
}
