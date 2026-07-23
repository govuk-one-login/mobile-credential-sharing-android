package uk.gov.onelogin.sharing.verifier.cancellation

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
class VerifierCancellationScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasResetJourney = false

    @Test
    fun `Validate UI contents`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Composable screen" to { Render() },
            "Preview" to { VerifierCancellationScreenPreview() }
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
            VerifierCancellationScreen(
                onCancel = { hasResetJourney = true }
            )
        }
    ) {
        GdsTheme { content() }
    }
}
