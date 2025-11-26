package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import kotlinx.coroutines.Dispatchers
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.holder.HolderWelcomeTexts.HOLDER_WELCOME_TEXT
import uk.gov.onelogin.sharing.holder.QrCodeGenerator.QR_CODE_CONTENT_DESC
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.SessionSecurityTestStub

class HolderWelcomeScreenRule(composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {

    private lateinit var content: () -> Unit
    private val fakeMdocSession = FakeMdocSessionManager()
    val dummyPublicKey = SessionSecurityTestStub.generateValidKeyPair()
    private val fakeSessionSecurity = FakeSessionSecurity(
        publicKey = dummyPublicKey
    )
    private val fakeEngagementGenerator = FakeEngagementGenerator(
        data = "mdoc:TEST_QR"
    )

    val viewModel: HolderWelcomeViewModel by lazy {
        HolderWelcomeViewModel(
            sessionSecurity = fakeSessionSecurity,
            engagementGenerator = fakeEngagementGenerator,
            mdocBleSession = fakeMdocSession,
            dispatcher = Dispatchers.Main
        )
    }

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
            HolderWelcomeScreen(
                modifier = modifier,
                viewModel = viewModel
            )
        }
    }
}
