package uk.gov.onelogin.sharing.holder.error

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.test.SemanticsNodeInteraction
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

abstract class ErrorScreenTemplateRule(
    composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources = ApplicationProvider.getApplicationContext<Context>()
        .resources
) : ComposeContentTestRule by composeTestRule {

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

    abstract fun assertValidUi(useUnmergedTree: Boolean = false)

    fun onButton(useUnmergedTree: Boolean = true) = onNodeWithText(
        resources.getString(R.string.holder_generic_error_button),
        useUnmergedTree = useUnmergedTree
    )

    abstract fun onTitle(useUnmergedTree: Boolean = false): SemanticsNodeInteraction

    fun performGoBackToDocumentsClick(useUnmergedTree: Boolean = true) = onButton(useUnmergedTree)
        .performClick()
}