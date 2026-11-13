package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.android.ui.theme.minimumTouchTarget
import uk.gov.onelogin.sharing.holder.R

class BleRestrictedScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    // node finders
    fun onTitle(useUnmergedTree: Boolean = false) = onNode(
        hasText(resources.getString(R.string.holder_ble_restricted_title))
            and isHeading(),
        useUnmergedTree = useUnmergedTree
    )

    fun onFirstParagraph(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_ble_restricted_body_1),
        useUnmergedTree = useUnmergedTree
    )

    fun onSecondParagraph(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_ble_restricted_body_2),
        useUnmergedTree = useUnmergedTree
    )

    fun onThirdParagraph(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_ble_restricted_body_3),
        useUnmergedTree = useUnmergedTree
    )

    fun onGoBackButton(useUnmergedTree: Boolean = false) = onNode(
        hasText(resources.getString(R.string.holder_ble_restricted_button))
            and hasRole(Role.Button),
        useUnmergedTree = useUnmergedTree
    )

    // assertions
    fun assertValidTitle(useUnmergedTree: Boolean = false) =
        onTitle(useUnmergedTree).assertIsDisplayed()

    fun assertValidBody1(useUnmergedTree: Boolean = false) =
        onFirstParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidBody2(useUnmergedTree: Boolean = false) =
        onSecondParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidBody3(useUnmergedTree: Boolean = false) =
        onThirdParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidButton(useUnmergedTree: Boolean = false) = onGoBackButton(useUnmergedTree)
        .assertHasClickAction()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidUi(useUnmergedTree: Boolean = false) {
        assertValidTitle(useUnmergedTree)
        assertValidBody1(useUnmergedTree)
        assertValidBody2(useUnmergedTree)
        assertValidBody3(useUnmergedTree)
        assertValidButton(useUnmergedTree)
    }

    fun assertWelshTitleIsDisplayed() =
        onNode(hasText("Mae Bluetooth wedi’i gyfyngu") and isHeading()).assertIsDisplayed()

    fun performGoBackClick() = onGoBackButton().performClick()
}
