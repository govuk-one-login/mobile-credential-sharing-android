package uk.gov.onelogin.sharing.holder.error.unfulfillable

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateRule

class UnfulfillableErrorScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>()
        .resources
) : ErrorScreenTemplateRule(composeTestRule, resources) {
    override fun assertValidUi(useUnmergedTree: Boolean) {
        assertValidButton(useUnmergedTree)
    }

    override fun onTitle(useUnmergedTree: Boolean): SemanticsNodeInteraction {
        TODO("Not yet implemented")
    }
}