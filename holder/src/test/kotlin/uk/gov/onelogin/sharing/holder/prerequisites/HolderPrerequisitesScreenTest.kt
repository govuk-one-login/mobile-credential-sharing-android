package uk.gov.onelogin.sharing.holder.prerequisites

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@RunWith(RobolectricTestParameterInjector::class)
class HolderPrerequisitesScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = HolderPrerequisitesScreenRule()

    @Test
    fun `Progress indicator is displayed`() = runTest {
        composeTestRule.setContent {
            Render()
        }

        composeTestRule.assertProgressIndicatorIsDisplayed()
    }

    @Test
    fun `Preview follows the same display as the main composable screen`() = runTest {
        composeTestRule.setContent {
            RenderPreview()
        }

        composeTestRule.assertProgressIndicatorIsDisplayed()
    }

    /**
     * Suppresses 'compose:vm-forwarding-check' as this is for testing purposes.
     */
    @Composable
    private fun Render() {
        HolderPrerequisitesScreen(modifier = Modifier.fillMaxSize())
    }

    @Composable
    private fun RenderPreview() {
        HolderPrerequisitesScreenPreview()
    }
}
