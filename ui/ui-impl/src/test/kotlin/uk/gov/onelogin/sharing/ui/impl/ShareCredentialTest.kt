package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter

@RunWith(AndroidJUnit4::class)
class ShareCredentialTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders holder flow`() {
        val appGraph = createTestAppGraph()
        val holderGraph = createTestHolderGraph(appGraph)
        val presenter = FakeCredentialPresenter(
            appGraph = appGraph,
            orchestrator = holderGraph.holderOrchestrator()
        )

        composeTestRule.setContent {
            ShareCredential(component = presenter)
        }

        composeTestRule.waitForIdle()
    }
}
