package uk.gov.onelogin.sharing.holder.loading

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.logging.api.v3.MemorisedLogger
import uk.gov.onelogin.sharing.analytics.SystemAnalyticsLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule

@RunWith(RobolectricTestParameterInjector::class)
class LoadingScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    private val viewModel by lazy {
        LoadingViewModel(
            analyticsLogger = SystemAnalyticsLogger(MemorisedLogger()),
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `Label for the type is displayed`(@TestParameter type: LoadingScreenType) =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.setContent {
                LoadingScreen(type = type, viewModel = viewModel)
            }

            composeTestRule
                .onNodeWithText(resources.getString(type.labelRes))
                .assertIsDisplayed()
        }

    @Test
    fun `Getting details preview follows the same display as the screen`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.setContent { LoadingScreenGettingDetailsPreview() }

            composeTestRule
                .onNodeWithText(
                    resources.getString(LoadingScreenType.GettingDetails.labelRes)
                )
                .assertIsDisplayed()
        }

    @Test
    fun `Sharing details preview follows the same display as the screen`() =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.setContent { RenderSharingDetailsPreview() }

            composeTestRule
                .onNodeWithText(
                    resources.getString(LoadingScreenType.SharingDetails.labelRes)
                )
                .assertIsDisplayed()
        }

    @Composable
    private fun RenderSharingDetailsPreview() {
        LoadingScreenSharingDetailsPreview()
    }
}
