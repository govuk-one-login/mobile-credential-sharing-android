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
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.orchestration.FakeCredentialProvider
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierConfigStub.verifierConfigStub
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.sdk.api.verifier.VerifyCredentialGraph

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    val appGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
        .create(
            applicationContext = ApplicationProvider.getApplicationContext(),
            logger = SystemLogger(),
            permissionCheckerV2 = { emptyList() }
        )

    val holderGraph = createGraphFactory<PresentCredentialGraph.Factory>()
        .create(appGraph = appGraph, credentialProvider = FakeCredentialProvider())

    val verifierGraph = createGraphFactory<VerifyCredentialGraph.Factory>()
        .create(
            appGraph = appGraph,
            verifierConfig = verifierConfigStub
        )

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

    @Test
    fun `opening verifier shows sharing dialog`() {
        composeTestRule.render()

        composeTestRule.openVerifier()

        composeTestRule.assertSharingDialogIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `Rotation is possible during the holder journey`() = runTest {
        val restorationTester = StateRestorationTester(composeTestRule)
        restorationTester.setContent {
            TestAppScreen(
                presentCredentialSdk = { _ ->
                    FakeCredentialPresenter(
                        appGraph = appGraph,
                        orchestrator = holderGraph.holderOrchestrator()
                    )
                },
                mockCredentials = listOf(
                    MockCredential(
                        id = "test-id",
                        displayName = "Jane Doe",
                        rawCredential = byteArrayOf(),
                        privateKey = byteArrayOf()
                    )
                ),
                verifyCredentialSdk = { _ ->
                    FakeCredentialVerifier(
                        appGraph = appGraph,
                        orchestrator = verifierGraph.verifierOrchestrator()
                    )
                }
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
