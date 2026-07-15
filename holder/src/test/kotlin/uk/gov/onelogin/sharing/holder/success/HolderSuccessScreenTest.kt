package uk.gov.onelogin.sharing.holder.success

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.holder.R

@RunWith(RobolectricTestParameterInjector::class)
class HolderSuccessScreenTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val resources =
        ApplicationProvider.getApplicationContext<Context>().resources

    @Test
    fun `Displays unfulfillable request title`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent { HolderSuccessScreen() }

        composeTestRule.onNodeWithText(
            resources.getString(R.string.holder_success_unfulfillable_request_title)
        ).assertIsDisplayed()
    }

    @Test
    fun `Preview renders without errors`() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent { HolderSuccessScreenPreview() }

        composeTestRule.onNodeWithText(
            resources.getString(R.string.holder_success_unfulfillable_request_title)
        ).assertIsDisplayed()
    }

    @Test
    fun `Back button is disabled and screen remains visible`() =
        runTest(dispatcherRule.testDispatcher) {
            lateinit var navController: TestNavHostController

            composeTestRule.setContent {
                val context = LocalContext.current
                navController = TestNavHostController(context).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                }

                NavHost(
                    navController = navController,
                    startDestination = "previous"
                ) {
                    composable("previous") {}
                    composable("success") {
                        HolderSuccessScreen()
                    }
                }
            }

            composeTestRule.runOnUiThread {
                navController.navigate("success")
            }
            composeTestRule.waitForIdle()

            composeTestRule.runOnUiThread {
                val activity = navController.context as ComponentActivity
                activity.onBackPressedDispatcher.onBackPressed()
            }
            composeTestRule.waitForIdle()

            assertEquals("success", navController.currentDestination?.route)
        }
}
