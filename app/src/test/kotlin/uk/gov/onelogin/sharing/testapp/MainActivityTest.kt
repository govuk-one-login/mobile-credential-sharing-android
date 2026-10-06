package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.api.analytics.parameters.data.TaxonomyLevel2
import uk.gov.logging.api.v3dot1.logger.asLegacyEvent
import uk.gov.logging.testdouble.analytics.FakeAnalyticsLogger
import uk.gov.onelogin.sharing.testapp.home.TestAppScreen
import uk.gov.onelogin.sharing.testapp.home.TestAppViewModel
import kotlin.test.assertTrue
import uk.gov.logging.api.v3dot1.model.ViewEvent.Screen
import uk.gov.logging.api.v3dot1.model.RequiredParameters

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeTestRule = MainActivityRule(
        composeTestRule = createComposeRule()
    )

    private val analyticsLogger = FakeAnalyticsLogger()

    private val viewModel by lazy {
        TestAppViewModel(analyticsLogger = analyticsLogger)
    }

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

    @Test
    fun `Launching the screen calls an analytics logger`() = runTest {
        val expectedEvent = Screen(
            name = "Credential Sharing Test App",
            id = "TestAppScreen",
            params = RequiredParameters(
                taxonomyLevel2 = TaxonomyLevel2.WALLET
            )
        ).asLegacyEvent()

        composeTestRule.setContent {
            Render()
        }
        composeTestRule.waitForIdle()

        assertTrue {
            expectedEvent in analyticsLogger
        }
    }

    @Composable
    fun Render() {
        TestAppScreen(
            viewModel = viewModel,
            onStartHolderJourney = { composeTestRule.updateStartHolderJourney() },
            onStartVerifierJourney = { composeTestRule.updateStartVerifierJourney() }
        )
    }
}
