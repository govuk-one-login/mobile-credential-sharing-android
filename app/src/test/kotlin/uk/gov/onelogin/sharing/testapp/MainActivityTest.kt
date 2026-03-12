package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metro.createGraphFactory
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialGraph
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.api.verifier.VerifierCredentialGraph
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter
import uk.gov.onelogin.sharing.sdk.FakeCredentialProvider
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    val appGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
        .create(
            applicationContext = ApplicationProvider.getApplicationContext(),
            logger = SystemLogger()
        )

    val holderGraph = createGraphFactory<PresenterCredentialGraph.Factory>()
        .create(appGraph = appGraph, FakeCredentialProvider())

    val verifierGraph = createGraphFactory<VerifierCredentialGraph.Factory>()
        .create(appGraph = appGraph)

    @get:Rule
    val composeTestRule = MainActivityRule(
        composeTestRule = createComposeRule(),
        appGraph = appGraph,
        holderGraph = holderGraph,
        verifierGraph = verifierGraph
    )

    @Test
    fun `test content`() {
        composeTestRule.render()

        composeTestRule.assertHolderIsDisplayed()
        composeTestRule.assertVerifierIsDisplayed()
    }

    @Test
    fun `opening holder shows sharing dialog`() {
        composeTestRule.render()

        composeTestRule.openHolder()

        composeTestRule.assertSharingDialogIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `Rotation is possible during the holder journey`() = runTest {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            TestAppScreen(
                credentialPresenter = FakeCredentialPresenter(
                    appGraph = appGraph,
                    orchestrator = holderGraph.holderOrchestrator()
                ),
                credentialVerifier = FakeCredentialVerifier(
                    appGraph = appGraph,
                    orchestrator = verifierGraph.verifierOrchestrator()
                )
            )
        }

        composeTestRule.openHolder()

        restorationTester.emulateSavedInstanceStateRestore()

        composeTestRule.assertSharingDialogIsDisplayed()
    }

    @Test
    fun `closing dialog hides sharing dialog`() {
        `opening holder shows sharing dialog`()

        composeTestRule.closeSharingDialog()

        composeTestRule.assertSharingDialogDoesNotExist()
    }
}
