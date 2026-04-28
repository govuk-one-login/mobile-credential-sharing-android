package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import org.hamcrest.Matcher
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers
import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeTexts.HOLDER_WELCOME_TEXT

class HolderWelcomeScreenRule(
    composeTestRule: ComposeContentTestRule,
) : ComposeContentTestRule by composeTestRule {

    private var onAwaitingUserConsent = false
    private var connectionError: BluetoothSessionError? = null
    private var onGenericError = false

    fun callOnAwaitingUserConsent(value: Boolean = true) {
        this.onAwaitingUserConsent = value
    }
    fun callOnConnectionError(value: BluetoothSessionError) {
        this.connectionError = value
    }
    fun callOnGenericError(value: Boolean = true) {
        this.onGenericError = value
    }

    fun assertOnAwaitingUserConsentIsCalled() = waitUntil { onAwaitingUserConsent }
    fun assertOnConnectionError(
        matcher: Matcher<in BluetoothSessionError>
    ) = waitUntil { matcher.matches(connectionError) }

    fun assertOnGenericErrorIsCalled() = waitUntil { onGenericError }

    fun assertWelcomeTextIsDisplayed() = onNodeWithText(HOLDER_WELCOME_TEXT).assertIsDisplayed()
    fun assertQrCodeIsDisplayed() = onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
        .assertIsDisplayed()
        .assert(SemanticsMatchers.hasRole(Role.Image))
}