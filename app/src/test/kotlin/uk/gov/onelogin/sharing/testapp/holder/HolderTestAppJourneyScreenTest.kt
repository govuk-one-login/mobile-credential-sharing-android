package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter

@RunWith(AndroidJUnit4::class)
class HolderTestAppJourneyScreenTest {

    @get:Rule
    val composeTestRule = HolderTestAppJourneyScreenRule(createComposeRule())

    private val presenter by lazy {
        FakeCredentialPresenter(
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

            assertPrerequisitesNotStartedTextIsDisplayed()
            performCloseJourneyClick()
            assertHasClosedJourney()
        }
    }

    @Composable
    private fun Render() {
        HolderTestAppJourneyScreen(
            component = presenter,
            onCloseJourney = composeTestRule::updateHasClosedJourney
        )
    }
}
