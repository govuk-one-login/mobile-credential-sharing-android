package uk.gov.onelogin.sharing.holder.error.generic

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.KotlinTestParameters.namedTestValues
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.holder.session.HolderSessionState
import uk.gov.onelogin.sharing.orchestration.session.SessionError
import uk.gov.onelogin.sharing.orchestration.session.SessionErrorReason
import kotlin.test.Test

@RunWith(RobolectricTestParameterInjector::class)
class GenericHolderErrorScreenTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule = createComposeRule()

    private var initialHolderState: HolderSessionState = HolderSessionState.Complete.Failed(
        error = SessionError(
            "This is a unit test",
            SessionErrorReason.InvalidSessionDataPayload
        )
    )

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialHolderState = MutableStateFlow(initialHolderState)
        )
    }

    private val viewModel by lazy {
        GenericHolderErrorViewModel(
            orchestrator = orchestrator,
        )
    }

    @Test
    fun `Is valid UI`(
        @TestParameter content: @Composable () -> Unit = namedTestValues(
            "Screen-level composable" to {
                GenericHolderErrorScreen(
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel,
                )
            },
            "Preview composable" to {
                GenericHolderErrorScreenPreview()
            }
        )
    ) = runTest {
        composeTestRule.run {
            setContent(content)

            onNodeWithText(
                resources.getString(
                    R.string.holder_generic_error_title
                ),
                useUnmergedTree = false
            )
                .assertExists()
                .assert(isHeading())
        }
    }
}