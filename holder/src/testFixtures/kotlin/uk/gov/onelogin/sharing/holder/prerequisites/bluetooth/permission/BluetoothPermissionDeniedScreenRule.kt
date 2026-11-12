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

    fun onTitle() = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_title))
            and isHeading()
    ).onFirst()

    fun onFirstParagraph() = onAllNodesWithText(
        resources.getString(R.string.holder_bluetooth_permission_denied_body_1)
    ).onFirst()

    fun onSecondParagraph() = onAllNodesWithText(
        resources.getString(R.string.holder_bluetooth_permission_denied_body_2)
    ).onFirst()

    fun onAllowButton() = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_allow_button))
            and hasRole(Role.Button)
    ).onFirst()

    fun onCancelButton() = onAllNodes(
        hasText(resources.getString(R.string.holder_bluetooth_permission_denied_cancel_button))
            and hasRole(Role.Button)
    ).onFirst()

    private val loadingSpinner = hasTestTag(BLUETOOTH_PERMISSION_DENIED_SPINNER_TEST_TAG)
    fun onLoadingSpinner() = onNode(loadingSpinner)

    fun assertTitleIsDisplayed() = onTitle().assertIsDisplayed()

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

    fun assertBodyIsDisplayed() {
        onFirstParagraph().assertIsDisplayed()
        onSecondParagraph().assertIsDisplayed()
    }

    fun assertAllowButtonIsDisplayed() = onAllowButton()
        .assertHasClickAction()
        .assertIsDisplayed()

    fun assertCancelButtonIsDisplayed() = onCancelButton()
        .assertHasClickAction()
        .assertIsDisplayed()

    fun performAllowClick() = onAllowButton().performClick()

    fun performCancelClick() = onCancelButton().performClick()
}
