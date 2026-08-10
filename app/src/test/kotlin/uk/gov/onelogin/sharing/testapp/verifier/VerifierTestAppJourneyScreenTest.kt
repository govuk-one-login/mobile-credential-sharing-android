package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.sdk.FakeVerificationSession

@RunWith(AndroidJUnit4::class)
class VerifierTestAppJourneyScreenTest {

    @get:Rule
    val composeTestRule = VerifierTestAppJourneyScreenRule(createComposeRule())

    private val session by lazy {
        FakeVerificationSession(
            orchestrator = FakeOrchestrator()
        )
    }

    @Test
    fun `Loads the 'VerifyCredential' journey`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            assertPrerequisitesProgressIndicatorIsDisplayed()
        }
    }

    @Composable
    private fun Render() {
        VerifierTestAppJourneyScreen(session = session)
    }
}
