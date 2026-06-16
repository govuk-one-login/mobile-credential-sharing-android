package uk.gov.onelogin.sharing.verifier.finish

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseStub

@RunWith(AndroidJUnit4::class)
class FinishedVerifierJourneyScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = FinishedVerifierJourneyScreenRule(createComposeRule())

    private var response: DeviceResponse = DeviceResponseStub.successWithDocuments

    @Test
    fun `Exits the journey via button click`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent {
                Render()
            }

            performExitJourneyClick()
            assertHasExitedJourney()
        }
    }

    @Test
    fun `Outputs the Document to the User`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            setContent {
                Render()
            }

            assertDocumentIsDisplayed(response.documents!![0])
        }
    }

    @Composable
    private fun Render() {
        FinishedVerifierJourneyScreen(
            response = response,
            onExitJourney = { composeTestRule.updateHasExitedJourney() }
        )
    }
}
