package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.holder.R

class TurnOnBluetoothScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    // node finders
    fun onTitle() = onNode(
        hasText(resources.getString(R.string.holder_turn_on_bluetooth_title))
            and isHeading()
    )

    fun onFirstParagraph() =
        onNodeWithText(resources.getString(R.string.holder_turn_on_bluetooth_body_1))

    fun onSecondParagraph() =
        onNodeWithText(resources.getString(R.string.holder_turn_on_bluetooth_body_2))

    fun onTurnOnBluetoothButton() = onNode(
        hasText(resources.getString(R.string.holder_turn_on_bluetooth_button))
            and hasRole(Role.Button)
    )

    private val loadingSpinner = hasTestTag(LOADING_SPINNER_TEST_TAG)
    fun onLoadingSpinner() = onNode(loadingSpinner)

    // assertions
    fun assertTitleIsDisplayed() = onTitle().assertIsDisplayed()

    fun assertWelshTitleIsDisplayed() =
        onNode(hasText("Troi Bluetooth ymlaen") and isHeading()).assertIsDisplayed()

    fun assertTitleDoesNotExist() = onTitle().assertDoesNotExist()

    fun assertLoadingSpinnerIsDisplayed() = onLoadingSpinner().assertIsDisplayed()

    fun waitUntilLoadingSpinnerExists() = waitUntil {
        onAllNodes(loadingSpinner).fetchSemanticsNodes().isNotEmpty()
    }

    fun assertBodyIsDisplayed() {
        onFirstParagraph().assertIsDisplayed()
        onSecondParagraph().assertIsDisplayed()
    }

    fun assertTurnOnBluetoothButtonIsDisplayed() = onTurnOnBluetoothButton()
        .assertHasClickAction()
        .assertIsDisplayed()

    fun performTurnOnBluetoothClick() = onTurnOnBluetoothButton().performClick()
}
