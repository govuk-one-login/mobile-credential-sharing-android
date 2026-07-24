package uk.gov.onelogin.sharing.verifier.finish

import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponse
import uk.gov.onelogin.sharing.models.mdoc.sessionEstablishment.deviceResponse.DeviceResponseStub
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@RunWith(RobolectricTestParameterInjector::class)
class FinishedVerifierJourneyScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = FinishedVerifierJourneyScreenRule(createComposeRule())

    private var response: DeviceResponse = DeviceResponseStub.successWithDocuments

    private val orchestrator by lazy {
        FakeOrchestrator()
    }

    private val viewModel by lazy {
        FinishedVerifierJourneyViewModel(
            orchestrator = orchestrator
        )
    }

    @Test
    fun `Exits the journey`(
        @TestParameter action: (
            OnBackPressedDispatcherOwner?,
            FinishedVerifierJourneyScreenRule
        ) -> Unit = namedTestValues(
            "Exit journey button" to { _, rule -> rule.performExitJourneyClick() },
            "Back press" to { dispatcher, _ ->
                dispatcher?.onBackPressedDispatcher?.onBackPressed()
            }
        )
    ) = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.run {
            var backPressedDispatcher: OnBackPressedDispatcherOwner? = null
            setContent {
                backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current
                Render()
            }

            action(backPressedDispatcher, composeTestRule)

            assertHasExitedJourney()
            assertThat(
                orchestrator.resetCount,
                equalTo(1)
            )
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
            viewModel = viewModel,
            onExitJourney = { composeTestRule.updateHasExitedJourney() }
        )
    }
}
