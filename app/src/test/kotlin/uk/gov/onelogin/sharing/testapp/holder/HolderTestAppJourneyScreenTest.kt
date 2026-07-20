package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import uk.gov.onelogin.sharing.sdk.FakeCredentialPresenter

@RunWith(RobolectricTestParameterInjector::class)
class HolderTestAppJourneyScreenTest {

    @get:Rule
    val composeTestRule = HolderTestAppJourneyScreenRule(createComposeRule())

    private var initialState: HolderSessionState = HolderSessionState.NotStarted

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialState)
        )
    }

    private val presenter by lazy {
        FakeCredentialPresenter(
            appGraph = mockk(relaxed = true),
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Tapping the close button overlay ends the journey`() = runTest {
        composeTestRule.run {
            setContent {
                Render()
            }

            assertPrerequisitesProgressIndicatorIsDisplayed()
            performCloseJourneyClick()
            assertHasClosedJourney()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Completed session states hide the close journey button`(
        @TestParameter state: HolderSessionState = namedTestValues(
            "Cancelled" to HolderSessionState.Complete.Cancelled,
            "Failed" to HolderSessionState.Complete.Failed(
                SessionError(
                    "Unit test",
                    SessionErrorReason.ServiceUuidNotFound
                )
            ),
            "Success" to HolderSessionState.Complete.Success()
        )
    ) = runTest {
        initialState = state
        composeTestRule.run {
            setContent {
                Render()
            }

            advanceUntilIdle()

            assertCloseJourneyButtonDoesNotExist()
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
