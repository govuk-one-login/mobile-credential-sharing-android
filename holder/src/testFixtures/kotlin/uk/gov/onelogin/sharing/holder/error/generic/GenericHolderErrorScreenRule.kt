package uk.gov.onelogin.sharing.holder.error.generic

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R

class GenericHolderErrorScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun assertValidTitle(useUnmergedTree: Boolean = false) = onTitle(useUnmergedTree)
        .assertExists()
        .assert(isHeading())

    fun onTitle(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_title),
        useUnmergedTree = useUnmergedTree
    )
}