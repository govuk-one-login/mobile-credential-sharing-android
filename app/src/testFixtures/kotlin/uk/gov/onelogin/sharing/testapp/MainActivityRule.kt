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
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph

class MainActivityRule(
    composeTestRule: ComposeContentTestRule,
    private val appGraph: CredentialSharingAppGraph,
    private val holderGraph: PresentCredentialGraph,
    private val verifierGraph: VerifyCredentialGraph,
    private val holderText: String,
    private val verifierText: String
) : ComposeContentTestRule by composeTestRule {

    constructor(
        composeTestRule: ComposeContentTestRule,
        appGraph: CredentialSharingAppGraph,
        holderGraph: PresentCredentialGraph,
        verifierGraph: VerifyCredentialGraph,
        resources: Resources = ApplicationProvider.getApplicationContext<Context>().resources
    ) : this(
        composeTestRule = composeTestRule,
        appGraph = appGraph,
        holderGraph = holderGraph,
        verifierGraph = verifierGraph,
        holderText = resources.getString(R.string.holder),
        verifierText = resources.getString(R.string.verifier)
    )

    fun render() {
        setContent {
            Content(
                appGraph = appGraph,
                credentialPresenter = holderGraph,
                credentialVerifier = verifierGraph
            )
        }
    }

    @Composable
    fun Content(
        appGraph: CredentialSharingAppGraph,
        credentialPresenter: PresentCredentialGraph,
        credentialVerifier: VerifyCredentialGraph
    ) {
        TestAppScreen(
            credentialPresenter = FakeCredentialPresenter(
                appGraph = appGraph,
                orchestrator = credentialPresenter.holderOrchestrator()
            ),
            credentialVerifier = FakeCredentialVerifier(
                appGraph = appGraph,
                orchestrator = credentialVerifier.verifierOrchestrator()
            )
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
