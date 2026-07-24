package uk.gov.onelogin.sharing.orchestration.cancellation

import androidx.compose.runtime.Composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.theme.m3.GdsTheme

@RunWith(AndroidJUnit4::class)
class CancellationDialogContentsTest {
    @get:Rule
    val composeTestRule = CancellationDialogContentsRule()

    @Test
    fun `Validate UI contents`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            assertTitleIsDisplayed()
            assertCancelJourneyButtonIsDisplayed()
            assertDismissDialogButtonIsDisplayed()
        }
    }

    @Test
    fun `Dialog is dismissable`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performDismissDialogClick()
            assertDismissDialogClickCount(1)
        }
    }

    @Test
    fun `Defers to lambda when cancelling the journey`() = runTest {
        composeTestRule.run {
            setContent { Render() }
            performCancelJourneyClick()
            assertCancelJourneyClickCount(1)
        }
    }

    @Composable
    private fun Render(
        content: @Composable () -> Unit = {
            CancellationDialogContents(
                onCancel = composeTestRule::incrementCancelJourneyClickCount,
                onDismiss = composeTestRule::incrementDismissDialogClickCount
            )
        },
    ) {
        GdsTheme { content() }
    }
}