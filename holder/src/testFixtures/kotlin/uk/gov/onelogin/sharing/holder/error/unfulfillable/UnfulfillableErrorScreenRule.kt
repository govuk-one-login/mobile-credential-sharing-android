package uk.gov.onelogin.sharing.holder.error.unfulfillable

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.error.ErrorScreenTemplateRule

class UnfulfillableErrorScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>()
        .resources
) : ErrorScreenTemplateRule(composeTestRule, resources) {
    fun assertValidBody1(useUnmergedTree: Boolean = false) = onBody1(useUnmergedTree)
        .assertExists()

    fun assertValidBody2(useUnmergedTree: Boolean = false) = onBody2(useUnmergedTree)
        .assertExists()

    override fun assertValidUi(useUnmergedTree: Boolean) {
        assertValidTitle(useUnmergedTree)
        assertValidBody1(useUnmergedTree)
        assertValidBody2(useUnmergedTree)
        assertValidButton(useUnmergedTree)
    }

    fun onBody1(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_unfulfillable_error_body_1),
        useUnmergedTree = useUnmergedTree
    )

    fun onBody2(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_unfulfillable_error_body_2),
        useUnmergedTree = useUnmergedTree
    )

    override fun onTitle(useUnmergedTree: Boolean): SemanticsNodeInteraction =
        onNodeWithText(
            resources.getString(
                R.string.holder_unfulfillable_error_title
            ),
            useUnmergedTree
        )
}