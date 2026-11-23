package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.android.ui.theme.minimumTouchTarget
import uk.gov.onelogin.sharing.holder.R

class BluetoothPermissionDeniedScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun onTitle(useUnmergedTree: Boolean = false) = onNodeWithText(
        resources.getString(R.string.holder_bluetooth_permission_denied_title),
        useUnmergedTree = useUnmergedTree
    )

    fun onFirstParagraph(useUnmergedTree: Boolean = false) = onAllNodesWithText(
        resources.getString(R.string.holder_bluetooth_permission_denied_body_1),
        useUnmergedTree = useUnmergedTree
    ).onFirst()

    fun onSecondParagraph(useUnmergedTree: Boolean = false) = onAllNodesWithText(
        resources.getString(R.string.holder_bluetooth_permission_denied_body_2),
        useUnmergedTree = useUnmergedTree
    ).onFirst()

    fun onAllowButton(useUnmergedTree: Boolean = false) = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_allow_button))
            and hasRole(Role.Button),
        useUnmergedTree = useUnmergedTree
    ).onFirst()

    fun onCancelButton(useUnmergedTree: Boolean = false) = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_cancel_button))
            and hasRole(Role.Button),
        useUnmergedTree = useUnmergedTree
    ).onFirst()

    // assertions
    fun assertValidTitle(useUnmergedTree: Boolean = false) = onTitle(useUnmergedTree)
        .assert(isHeading())
        .assertIsDisplayed()

    fun assertValidBody1(useUnmergedTree: Boolean = false) =
        onFirstParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidBody2(useUnmergedTree: Boolean = false) =
        onSecondParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidAllowButton(useUnmergedTree: Boolean = false) = onAllowButton(useUnmergedTree)
        .assertHasClickAction()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidCancelButton(useUnmergedTree: Boolean = false) = onCancelButton(useUnmergedTree)
        .assertHasClickAction()
        .assertIsDisplayed()
        .assertWidthIsAtLeast(minimumTouchTarget)
        .assertHeightIsAtLeast(minimumTouchTarget)

    fun assertValidUi(useUnmergedTree: Boolean = false) {
        assertValidTitle(useUnmergedTree)
        assertValidBody1(useUnmergedTree)
        assertValidBody2(useUnmergedTree)
        assertValidAllowButton(useUnmergedTree)
        assertValidCancelButton(useUnmergedTree)
    }

    private val title =
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_title)) and
            isHeading()

    fun assertTitleDoesNotExist() = onAllNodes(title).assertCountEquals(0)

    fun waitUntilTitleDoesNotExist() = waitUntil {
        onAllNodes(title).fetchSemanticsNodes().isEmpty()
    }

    fun performAllowClick() = onAllowButton().performClick()

    fun performCancelClick() = onCancelButton().performClick()
}
