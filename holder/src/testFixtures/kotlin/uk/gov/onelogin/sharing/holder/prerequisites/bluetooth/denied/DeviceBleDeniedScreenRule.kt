package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.denied

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
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.holder.R

class DeviceBleDeniedScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    fun onTitle() = onNode(
        hasText(resources.getString(R.string.holder_device_ble_denied_title)) and isHeading()
    )

    fun onFirstParagraph() =
        onNodeWithText(resources.getString(R.string.holder_device_ble_denied_body_1))

    fun onSecondParagraph() =
        onNodeWithText(resources.getString(R.string.holder_device_ble_denied_body_2))

    fun onTurnOnBluetoothButton() = onAllNodes(
        hasText(resources.getString(R.string.holder_device_ble_denied_turn_on_button))
            and hasRole(Role.Button)
    ).onFirst()

    fun onCancelButton() = onAllNodes(
        hasText(resources.getString(R.string.holder_device_ble_denied_cancel_button))
            and hasRole(Role.Button)
    ).onFirst()

    private val loadingSpinner = hasTestTag(DEVICE_BLE_DENIED_SPINNER_TEST_TAG)
    fun onLoadingSpinner() = onNode(loadingSpinner)

    fun assertTitleIsDisplayed() = onTitle().assertIsDisplayed()

    fun assertWelshTitleIsDisplayed() =
        onNode(hasText("Mae Bluetooth wedi’i droi i ffwrdd") and isHeading()).assertIsDisplayed()

    fun assertTitleDoesNotExist() = onTitle().assertDoesNotExist()

    fun assertLoadingSpinnerIsDisplayed() = onLoadingSpinner().assertIsDisplayed()

    fun waitUntilLoadingSpinnerExists() = waitUntil {
        onAllNodes(loadingSpinner).fetchSemanticsNodes().isNotEmpty()
    }

    fun assertBodyIsDisplayed() {
        onFirstParagraph().assertIsDisplayed()
        onSecondParagraph().assertIsDisplayed()
    }

    fun assertButtonsAreDisplayed() {
        onTurnOnBluetoothButton().assertHasClickAction().assertIsDisplayed()
        onCancelButton().assertHasClickAction().assertIsDisplayed()
    }

    fun assertValidUi() {
        assertTitleIsDisplayed()
        assertBodyIsDisplayed()
        assertButtonsAreDisplayed()
    }

    fun performTurnOnBluetoothClick() = onTurnOnBluetoothButton().performClick()

    fun performCancelClick() = onCancelButton().performClick()
}
