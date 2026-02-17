package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination

@RunWith(AndroidJUnit4::class)
class CredentialSharingUiNavHostTest {
    @get:Rule
    val navHostTestRule = CredentialSharingUiNavHostRule(
        composeTestRule = createComposeRule(),
        uiGraph = createUiTestGraph()
    )

    @Test
    fun `holder start destination`() {
        navHostTestRule.render(
            startDestination = CredentialSharingDestination.Holder
        )

        navHostTestRule.assertCurrentRoute(CredentialSharingDestination.Holder::class)
    }

    @Test
    fun `verifier start destination`() {
        navHostTestRule.render(
            startDestination = CredentialSharingDestination.Verifier
        )

        navHostTestRule.assertCurrentRoute(CredentialSharingDestination.Verifier::class)
    }
}
