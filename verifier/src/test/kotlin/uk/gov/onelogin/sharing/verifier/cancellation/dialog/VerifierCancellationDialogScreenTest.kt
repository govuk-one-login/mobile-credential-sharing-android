package uk.gov.onelogin.sharing.verifier.cancellation.dialog

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.SemanticsNodeInteraction
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.android.ui.theme.m3.GdsTheme

@RunWith(RobolectricTestParameterInjector::class)
class VerifierCancellationDialogScreenTest {
    @get:Rule
    val composeTestRule = VerifierCancellationDialogContentsRule()

    @Test
    fun `Validate UI contents`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Composable screen" to { Render() },
            "Preview" to { VerifierCancellationDialogContentsPreview() }
        )
    ) = runTest {
        composeTestRule.run {
            setContent { content() }
            assertTitleIsDisplayed()
            assertCancelJourneyButtonIsDisplayed()
            assertDismissDialogButtonIsDisplayed()
        }
    }

    @Test
    fun `UI buttons defer to lambdas`(
        @TestParameter input: Pair<
            VerifierCancellationDialogContentsRule.() -> SemanticsNodeInteraction,
            VerifierCancellationDialogContentsRule.(Matcher<in Int>) -> Unit
            > = namedTestValues(
            "Cancel journey button" to (
                VerifierCancellationDialogContentsRule::performCancelJourneyClick to
                    VerifierCancellationDialogContentsRule::assertCancelJourneyClickCount
                ),
            "Dismiss dialog button" to (
                VerifierCancellationDialogContentsRule::performDismissDialogClick to
                    VerifierCancellationDialogContentsRule::assertDismissDialogClickCount
                )
        )
    ) = runTest {
        val (action, assertion) = input
        composeTestRule.run {
            setContent { Render() }
            action(this)
            assertion(this, equalTo(1))
        }
    }

    @Composable
    private fun Render(
        content: @Composable () -> Unit = {
            VerifierCancellationDialogContents(
                onCancelJourney = composeTestRule::incrementCancelJourneyClickCount,
                onDismiss = composeTestRule::incrementDismissDialogClickCount
            )
        }
    ) {
        GdsTheme { content() }
    }
}
