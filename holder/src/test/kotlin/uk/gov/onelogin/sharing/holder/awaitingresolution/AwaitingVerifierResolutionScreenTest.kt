package uk.gov.onelogin.sharing.holder.awaitingresolution

import androidx.compose.runtime.Composable
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@RunWith(RobolectricTestParameterInjector::class)
class AwaitingVerifierResolutionScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = AwaitingVerifierResolutionScreenRule()

    @Test
    fun `Title is displayed`() = runTest {
        composeTestRule.setContent {
            Render()
        }

        composeTestRule.assertTitleIsDisplayed()
    }

    @Test
    fun `Preview follows the same display as the main composable screen`() = runTest {
        composeTestRule.setContent {
            RenderPreview()
        }

        composeTestRule.assertTitleIsDisplayed()
    }

    @Composable
    private fun Render() {
        AwaitingVerifierResolutionScreen()
    }

    @Composable
    private fun RenderPreview() {
        AwaitingVerifierResolutionScreenPreview()
    }
}
