package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import uk.gov.android.ui.componentsv2.matchers.SemanticsMatchers.hasRole
import uk.gov.onelogin.sharing.holder.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen

class HolderWelcomeScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    private lateinit var content: () -> Unit

    fun assertWelcomeTextIsDisplayed() = onNodeWithText(HOLDER_WELCOME_TEXT).assertIsDisplayed()

    fun assertQrCodeIsDisplayed() = onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
        .assertIsDisplayed()
        .assert(hasRole(Role.Image))

    fun render(modifier: Modifier = Modifier) {
        setContent {
            HolderWelcomeScreen(modifier = modifier)
        }
    }
}
