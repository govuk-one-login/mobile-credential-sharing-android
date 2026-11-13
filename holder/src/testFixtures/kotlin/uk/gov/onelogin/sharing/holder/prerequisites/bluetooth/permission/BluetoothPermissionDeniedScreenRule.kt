package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.permission

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.holder.R

class BluetoothPermissionDeniedScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun onTitle(useUnmergedTree: Boolean = false) = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_title))
            and isHeading(),
        useUnmergedTree = useUnmergedTree
    ).onFirst()

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

    private val loadingSpinner = hasTestTag(BLUETOOTH_PERMISSION_DENIED_SPINNER_TEST_TAG)
    fun onLoadingSpinner() = onNode(loadingSpinner)

    // assertions
    fun assertValidTitle(useUnmergedTree: Boolean = false) =
        onTitle(useUnmergedTree).assertIsDisplayed()

    fun assertValidBody1(useUnmergedTree: Boolean = false) =
        onFirstParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidBody2(useUnmergedTree: Boolean = false) =
        onSecondParagraph(useUnmergedTree).assertIsDisplayed()

    fun assertValidAllowButton(useUnmergedTree: Boolean = false) = onAllowButton(useUnmergedTree)
        .assertHasClickAction()
        .assertIsDisplayed()

    fun assertValidCancelButton(useUnmergedTree: Boolean = false) = onCancelButton(useUnmergedTree)
        .assertHasClickAction()
        .assertIsDisplayed()

    fun assertValidUi(useUnmergedTree: Boolean = false) {
        assertValidTitle(useUnmergedTree)
        assertValidBody1(useUnmergedTree)
        assertValidBody2(useUnmergedTree)
        assertValidAllowButton(useUnmergedTree)
        assertValidCancelButton(useUnmergedTree)
    }

    fun assertWelshTitleIsDisplayed() = onAllNodes(
        hasText("Mae angen i chi ganiatáu cysylltiad â dyfeisiau cyfagos") and isHeading()
    ).onFirst().assertIsDisplayed()

    fun assertTitleDoesNotExist() = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_title))
            and isHeading()
    ).assertCountEquals(0)

    fun assertLoadingSpinnerIsDisplayed() = onLoadingSpinner().assertIsDisplayed()

    fun waitUntilLoadingSpinnerExists() = waitUntil {
        onAllNodes(loadingSpinner).fetchSemanticsNodes().isNotEmpty()
    }

    fun performAllowClick() = onAllowButton().performClick()

    fun performCancelClick() = onCancelButton().performClick()
}
