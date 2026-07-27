package uk.gov.onelogin.sharing.orchestration.cancellation

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme

@RunWith(RobolectricTestParameterInjector::class)
class CancellationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasResetJourney = false

    @Test
    fun `Validate UI contents`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen" to { Render() },
            "Preview" to {
                Render { CancellationScreenPreview() }
            }
        )
    ) = runTest {
        composeTestRule.run {
            setContent { content() }
            onNodeWithTag("progressIndicator").assertExists()
        }
    }

    @Test
    fun `Calls 'onCancel' when launching the screen`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            waitUntil { hasResetJourney }
        }
    }

    @Composable
    private fun Render(
        content: @Composable () -> Unit = {
            CancellationScreen(
                onCancel = { hasResetJourney = true }
            )
        }
    ) {
        GdsTheme { content() }
    }
}
