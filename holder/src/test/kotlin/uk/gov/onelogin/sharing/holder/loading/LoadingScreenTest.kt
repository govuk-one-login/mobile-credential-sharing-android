package uk.gov.onelogin.sharing.holder.loading

import android.content.Context
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
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
    fun `Back press is swallowed when cancellation is unavailable`() =
        runTest(dispatcherRule.testDispatcher) {
            lateinit var navController: TestNavHostController
            var backDispatcher: OnBackPressedDispatcherOwner? = null

            composeTestRule.setContent {
                backDispatcher = LocalOnBackPressedDispatcherOwner.current
                navController = TestNavHostController(LocalContext.current).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
                NavHost(navController = navController, startDestination = "previous") {
                    composable("previous") {}
                    composable("loading") {
                        LoadingScreen(
                            type = LoadingScreenType.SharingDetails,
                            cancellationAvailable = false,
                            viewModel = viewModel
                        )
                    }
                }
            }

            composeTestRule.runOnUiThread { navController.navigate("loading") }
            composeTestRule.waitForIdle()

            composeTestRule.runOnUiThread {
                backDispatcher!!.onBackPressedDispatcher.onBackPressed()
            }
            composeTestRule.waitForIdle()

            assertThat(navController.currentDestination?.route, equalTo("loading"))
        }

    @Test
    fun `Back press is not swallowed when cancellation is available`() =
        runTest(dispatcherRule.testDispatcher) {
            lateinit var navController: TestNavHostController
            var backDispatcher: OnBackPressedDispatcherOwner? = null

            composeTestRule.setContent {
                backDispatcher = LocalOnBackPressedDispatcherOwner.current
                navController = TestNavHostController(LocalContext.current).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }
                NavHost(navController = navController, startDestination = "previous") {
                    composable("previous") {}
                    composable("loading") {
                        LoadingScreen(
                            type = LoadingScreenType.GettingDetails,
                            cancellationAvailable = true,
                            viewModel = viewModel
                        )
                    }
                }
            }

            composeTestRule.runOnUiThread { navController.navigate("loading") }
            composeTestRule.waitForIdle()

            composeTestRule.runOnUiThread {
                backDispatcher!!.onBackPressedDispatcher.onBackPressed()
            }
            composeTestRule.waitForIdle()

            assertThat(navController.currentDestination?.route, equalTo("previous"))
        }

    @Test
    fun `Label for the type is displayed`(@TestParameter type: LoadingScreenType) =
        runTest(dispatcherRule.testDispatcher) {
            composeTestRule.setContent {
                LoadingScreen(
                    type = type,
                    cancellationAvailable = false,
                    viewModel = viewModel
                )
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
