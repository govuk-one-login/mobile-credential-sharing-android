package uk.gov.onelogin.sharing.holder.prerequisites.bluetooth.errors

import android.content.Context
import android.content.res.Resources
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assertHasClickAction
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

class BleRestrictedScreenRule(
    private val composeTestRule: ComposeContentTestRule = createComposeRule(),
    private val resources: Resources =
        ApplicationProvider.getApplicationContext<Context>().resources
) : ComposeContentTestRule by composeTestRule {

    // node finders
    fun onTitle() = onNode(
        hasText(resources.getString(R.string.holder_ble_restricted_title))
            and isHeading()
    )

    fun onFirstParagraph() =
        onNodeWithText(resources.getString(R.string.holder_ble_restricted_body_1))

    fun onSecondParagraph() =
        onNodeWithText(resources.getString(R.string.holder_ble_restricted_body_2))

    fun onThirdParagraph() =
        onNodeWithText(resources.getString(R.string.holder_ble_restricted_body_3))

    fun onGoBackButton() = onNode(
        hasText(resources.getString(R.string.holder_ble_restricted_button))
            and hasRole(Role.Button)
    )

    // assertions
    fun assertTitleIsDisplayed() = onTitle().assertIsDisplayed()

    fun assertWelshTitleIsDisplayed() =
        onNode(hasText("Mae Bluetooth wedi’i gyfyngu") and isHeading()).assertIsDisplayed()

    fun assertBodyIsDisplayed() {
        onFirstParagraph().assertIsDisplayed()
        onSecondParagraph().assertIsDisplayed()
        onThirdParagraph().assertIsDisplayed()
    }

    fun assertGoBackButtonIsDisplayed() = onGoBackButton()
        .assertHasClickAction()
        .assertIsDisplayed()

    fun performGoBackClick() = onGoBackButton().performClick()
}
