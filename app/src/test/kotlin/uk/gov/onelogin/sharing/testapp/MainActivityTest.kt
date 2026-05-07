package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.home.TestAppScreen

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = MainActivityRule(
        composeTestRule = createComposeRule()
    )

    @Test
    fun `test content`() {
        composeTestRule.setContent {
            Render()
        }
        composeTestRule.assertHolderIsDisplayed()
        composeTestRule.assertVerifierIsDisplayed()
    }

    @Test
    fun `opening holder starts the holder journey`() {
        composeTestRule.setContent {
            Render()
        }
        composeTestRule.openHolder()

        composeTestRule.assertHolderJourneyHasStarted()
    }

    @Test
    fun `opening verifier starts the verifier journey`() {
        composeTestRule.setContent {
            Render()
        }
        composeTestRule.openVerifier()

        composeTestRule.assertVerifierJourneyHasStarted()
    }

    @Composable
    fun Render() {
        TestAppScreen(
            onStartHolderJourney = { composeTestRule.updateStartHolderJourney() },
            onStartVerifierJourney = { composeTestRule.updateStartVerifierJourney() }
        )
    }
}
