package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertIsDisplayed
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

    private val turnOnBluetoothText = resources.getString(R.string.holder_turn_on_bluetooth)

    fun assertTitleIsDisplayed() =
        onNode(hasText(turnOnBluetoothText) and isHeading())
            .assertIsDisplayed()

    fun assertBodyIsDisplayed() {
        onNodeWithText(resources.getString(R.string.holder_turn_on_bluetooth_body_1))
            .assertIsDisplayed()
        onNodeWithText(resources.getString(R.string.holder_turn_on_bluetooth_body_2))
            .assertIsDisplayed()
    }

    fun assertTurnOnBluetoothButtonIsDisplayed() =
        onNode(hasText(turnOnBluetoothText) and hasRole(Role.Button))
            .assertIsDisplayed()

    fun performTurnOnBluetoothClick() =
        onNode(hasText(turnOnBluetoothText) and hasRole(Role.Button))
            .performClick()
}
