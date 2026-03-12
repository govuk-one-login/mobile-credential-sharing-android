package uk.gov.onelogin.sharing.ui.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifyCredentialTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `renders verifier flow`() {
        val appGraph = createTestAppGraph()
        val verifierGraph = createTestVerifierGraph(appGraph)
        val verifier = FakeCredentialVerifier(
            appGraph = appGraph,
            orchestrator = verifierGraph.verifierOrchestrator()
        )

        composeTestRule.setContent {
            VerifyCredential(component = verifier)
        }

        composeTestRule.waitForIdle()
    }
}
