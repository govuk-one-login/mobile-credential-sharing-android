package uk.gov.onelogin.sharing.holder.error.generic

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.theme.minimumTouchTarget
import uk.gov.onelogin.sharing.holder.R

class GenericHolderErrorScreenRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider
        .getApplicationContext<Context>()
        .resources
) : ComposeContentTestRule by composeTestRule {

    fun assertValidBody1(useUnmergedTree: Boolean = false) = onBody1(useUnmergedTree)
        .assertExists()

    fun assertValidBody2(useUnmergedTree: Boolean = false) = onBody2(useUnmergedTree)
        .assertExists()

    fun assertValidButton(useUnmergedTree: Boolean = true) = onButton(useUnmergedTree)
        .assertExists()
        .assertHasClickAction()
        .assertIsEnabled()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidTitle(useUnmergedTree: Boolean = false) = onTitle(useUnmergedTree)
        .assertExists()
        .assert(isHeading())

    fun assertValidUi(useUnmergedTree: Boolean = false) {
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

    fun onButton(useUnmergedTree: Boolean = true) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_button),
        useUnmergedTree = useUnmergedTree
    )

    fun onTitle(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_title),
        useUnmergedTree = useUnmergedTree
    )

    fun performGoBackToDocumentsClick(useUnmergedTree: Boolean = true) = onButton(useUnmergedTree)
        .performClick()
}
