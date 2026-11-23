package uk.gov.onelogin.sharing.holder.error.generic

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateRule

class GenericHolderErrorScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider
        .getApplicationContext<Context>()
        .resources
) : ErrorScreenTemplateRule(composeTestRule, resources) {

    fun assertValidBody1(useUnmergedTree: Boolean = false) = onBody1(useUnmergedTree)
        .assertExists()

    fun assertValidBody2(useUnmergedTree: Boolean = false) = onBody2(useUnmergedTree)
        .assertExists()

    override fun assertValidUi(useUnmergedTree: Boolean) {
        assertValidBody1(useUnmergedTree)
        assertValidBody2(useUnmergedTree)
        assertValidButton(useUnmergedTree)
        assertValidTitle(useUnmergedTree)
    }

    fun onBody1(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_body_1),
        useUnmergedTree = useUnmergedTree
    )

    fun onBody2(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_body_2),
        useUnmergedTree = useUnmergedTree
    )

    override fun onTitle(useUnmergedTree: Boolean) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_title),
        useUnmergedTree = useUnmergedTree
    )
}
