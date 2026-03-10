package uk.gov.onelogin.sharing.testapp

import android.content.Context
import android.content.res.Resources
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.impl.FakeCredentialPresenter
import uk.gov.onelogin.sharing.ui.impl.FakeCredentialVerifier

class MainActivityRule(
    private val appGraph: CredentialSharingAppGraph,
    composeTestRule: ComposeContentTestRule,
    private val holderText: String,
    private val verifierText: String
) : ComposeContentTestRule by composeTestRule {

    constructor(
        composeTestRule: ComposeContentTestRule,
        appGraph: CredentialSharingAppGraph,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeTestRule = composeTestRule,
        appGraph = appGraph,
        holderText = resources.getString(R.string.holder),
        verifierText = resources.getString(R.string.verifier)
    )

    fun render() {
        setContent {
            Content(appGraph)
        }
    }

    @Composable
    fun Content(appGraph: CredentialSharingAppGraph) {
        TestAppScreen(
            credentialPresenter = FakeCredentialPresenter(appGraph),
            credentialVerifier = FakeCredentialVerifier(appGraph)
        )
    }

    fun assertHolderIsDisplayed() {
        onNodeWithText(holderText).isDisplayed()
    }

    fun assertVerifierIsDisplayed() {
        onNodeWithText(verifierText).isDisplayed()
    }

    fun openHolder() {
        onNodeWithText(holderText)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    fun assertSharingDialogIsDisplayed() {
        onNodeWithTag(SHARING_DIALOG_TAG)
            .assertExists()
            .assertIsDisplayed()
    }

    fun closeSharingDialog() {
        onNodeWithTag(CLOSE_DIALOG_BUTTON_TAG)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    fun assertSharingDialogDoesNotExist() {
        onNodeWithTag(SHARING_DIALOG_TAG).assertDoesNotExist()
    }
}
