package uk.gov.onelogin.sharing.orchestration.error

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason

@RunWith(AndroidJUnit4::class)
class UnrecoverableErrorContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private var hasExitedJourney: Boolean = false

    @Test
    fun `Tapping the exit journey button calls 'onExitJourney' lambda`() = runTest {
        composeTestRule.run {
            setContent {
                UnrecoverableErrorContent(
                    failureState = SessionError(
                        "This is a unit test",
                        SessionErrorReason.UnrecoverableThrowable(Exception())
                    ),
                    onExitJourney = { hasExitedJourney = true }
                )
            }

            onNodeWithText("Exit journey", useUnmergedTree = true).performClick()

            waitUntil { hasExitedJourney }
        }
    }
}
