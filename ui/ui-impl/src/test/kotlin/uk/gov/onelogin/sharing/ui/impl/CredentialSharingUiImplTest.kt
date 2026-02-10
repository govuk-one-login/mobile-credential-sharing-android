package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import uk.gov.onelogin.sharing.CredentialSharingSdk
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination

@RunWith(RobolectricTestRunner::class)
class CredentialSharingUiImplTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockSdk: CredentialSharingSdk = mockk(relaxed = true)

    @Test
    fun `renders hello world`() {
        val ui = CredentialSharingUiImpl()

        composeTestRule.setContent {
            ui.Render(
                sdk = mockSdk,
                startDestination = CredentialSharingDestination.CredentialSharingRoot
            )
        }

        composeTestRule.onNodeWithText("Hello World!").assertIsDisplayed()
    }
}
