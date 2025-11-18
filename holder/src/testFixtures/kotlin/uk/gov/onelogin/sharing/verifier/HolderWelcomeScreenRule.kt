package uk.gov.onelogin.sharing.verifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.verifier.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.verifier.QrCodeGenerator.QR_CODE_CONTENT_DESC

class HolderWelcomeScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    fun assertWelcomeTextIsDisplayed() = onNodeWithText(HOLDER_WELCOME_TEXT).assertIsDisplayed()

    fun assertQrCodeIsDisplayed() = onNodeWithContentDescription(QR_CODE_CONTENT_DESC)
        .assertIsDisplayed()
        // Replace with GOV.UK UI components V2 test fixture `hasRole` when applicable.
        .assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.Role,
                Role.Image
            )
        )

    fun render(modifier: Modifier = Modifier) {
        setContent {
            HolderWelcomeScreen(modifier = modifier)
        }
    }
}
