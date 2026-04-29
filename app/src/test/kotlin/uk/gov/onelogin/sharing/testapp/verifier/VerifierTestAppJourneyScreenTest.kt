package uk.gov.onelogin.sharing.testapp.verifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.sdk.FakeCredentialVerifier

@RunWith(AndroidJUnit4::class)
class VerifierTestAppJourneyScreenTest {

    @get:Rule
    val composeTestRule = VerifierTestAppJourneyScreenRule(createComposeRule())

    private val verifier by lazy {
        FakeCredentialVerifier(
            appGraph = mockk(relaxed = true),
            orchestrator = FakeOrchestrator()
        )
    }

    @Test
    fun `Tapping the close button overlay ends the journey`() = runTest {
        composeTestRule.run {
            setContent {
                Render()
            }

            performCloseJourneyClick()
            assertHasClosedJourney()
        }
    }

    @Composable
    private fun Render() {
        VerifierTestAppJourneyScreen(
            verifier = verifier,
            onCloseJourney = composeTestRule::updateHasClosedJourney
        )
    }
}
