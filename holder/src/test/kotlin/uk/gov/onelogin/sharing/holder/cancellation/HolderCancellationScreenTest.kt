package uk.gov.onelogin.sharing.holder.cancellation

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
class HolderCancellationScreenTest {
    @get:Rule
    val composeTestRule = HolderCancellationScreenRule()

    @Test
    fun `Validate UI contents`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Composable screen" to { Render() },
            "Preview" to { HolderCancellationScreenPreview() },
        ),
    ) = runTest {
        composeTestRule.run {
            setContent { content() }
            assertTitleIsDisplayed()
            assertCancelJourneyButtonIsDisplayed()
        }
    }

    @Test
    fun `UI buttons defer to lambdas`(
        @TestParameter input: Pair<
                HolderCancellationScreenRule.() -> SemanticsNodeInteraction,
                HolderCancellationScreenRule.(Matcher<in Int>) -> Unit
                > = namedTestValues(
            "Cancel journey button" to (
                    HolderCancellationScreenRule::performCancelJourneyClick to
                            HolderCancellationScreenRule::assertCancelJourneyClickCount
                    ),
        ),
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
            HolderCancellationScreen(
                onCancelJourney = composeTestRule::incrementCancelJourneyClickCount,
            )
        },
    ) {
        GdsTheme { content() }
    }
}