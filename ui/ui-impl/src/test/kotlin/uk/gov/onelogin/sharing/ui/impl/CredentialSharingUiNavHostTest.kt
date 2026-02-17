package uk.gov.onelogin.sharing.ui.impl

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.zacsweers.metro.createGraphFactory
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.Logger
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.api.CredentialSharingDestination
import uk.gov.onelogin.sharing.ui.impl.di.CredentialSharingUiGraph

@RunWith(AndroidJUnit4::class)
class CredentialSharingUiNavHostTest {
    fun createTestGraph(
        applicationContext: Context = ApplicationProvider.getApplicationContext(),
        logger: Logger = SystemLogger()
    ): CredentialSharingAppGraph = createGraphFactory<CredentialSharingAppGraph.Factory>()
        .create(
            applicationContext = applicationContext,
            logger = logger
        )

    val appGraph = createTestGraph()
    val uiGraph = createGraphFactory<CredentialSharingUiGraph.Factory>()
        .create(appGraph)

    @get:Rule
    val navHostTestRule = CredentialSharingUiNavHostRule(
        composeTestRule = createComposeRule(),
        uiGraph = uiGraph
    )

    @Test
    fun `holder start destination`() {
        navHostTestRule.renderWithController(
            startDestination = CredentialSharingDestination.HolderRoot
        )

        navHostTestRule.assertCurrentRoute(CredentialSharingDestination.HolderRoot::class)
    }

    @Test
    fun `verifier start destination`() {
        navHostTestRule.renderWithController(
            startDestination = CredentialSharingDestination.VerifierRoot
        )

        navHostTestRule.assertCurrentRoute(CredentialSharingDestination.VerifierRoot::class)
    }

    @Test
    fun `verifier dev menu destination`() {
        navHostTestRule.renderWithController(
            startDestination = CredentialSharingDestination.DevMenu
        )

        navHostTestRule.assertCurrentRoute(CredentialSharingDestination.DevMenu::class)
    }
}
