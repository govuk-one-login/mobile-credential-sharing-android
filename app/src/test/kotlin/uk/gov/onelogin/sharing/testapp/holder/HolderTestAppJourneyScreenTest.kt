package uk.gov.onelogin.sharing.testapp.holder

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
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
    fun `Loads the 'ShareCredential' journey`() = runTest {
        composeTestRule.run {
            setContent { Render() }

            assertPrerequisitesProgressIndicatorIsDisplayed()
        }
    }

    @Composable
    private fun Render() {
        HolderTestAppJourneyScreen(component = presenter)
    }
}
