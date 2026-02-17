package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

class MainActivityRule(
    private val appGraph: CredentialSharingAppGraph,
    composeTestRule: ComposeContentTestRule
) : ComposeContentTestRule by composeTestRule {

    fun render() {
        setContent {
            Content(appGraph)
        }
    }

    @Composable
    fun Content(appGraph: CredentialSharingAppGraph) {
        TestAppScreen(
            ui = FakeCredentialSharingUi(),
            sdk = FakeCredentialSharingSdk(appGraph)
        )
    }
}
