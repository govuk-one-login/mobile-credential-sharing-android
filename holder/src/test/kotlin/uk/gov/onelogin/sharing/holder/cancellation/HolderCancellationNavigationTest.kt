package uk.gov.onelogin.sharing.holder.cancellation

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.cancellation.HolderCancellationNavigationExt.configureHolderUserCancellationDialog
import uk.gov.onelogin.sharing.holder.cancellation.HolderCancellationNavigationExt.navigateToHolderUserCancellationDialog

@RunWith(AndroidJUnit4::class)
class HolderCancellationNavigationTest {

    @get:Rule
    val composeTestRule = HolderCancellationScreenRule()

    private lateinit var controller: TestNavHostController

    @Test
    fun `Cancellation dialog exists in the navigation graph`() {
        composeTestRule.run {
            setContent {
                controller = TestNavHostController(LocalContext.current).apply {
                    navigatorProvider.addNavigator(ComposeNavigator())
                    navigatorProvider.addNavigator(DialogNavigator())
                }

                Render()
            }

            onNodeWithText("Navigate").onParent().performClick()

            waitUntil {
                allOf(
                    instanceOf(HolderCancellationRoute::class.java),
                    not(nullValue(HolderCancellationRoute::class.java))
                ).matches(
                    controller.currentBackStackEntry?.toRoute<HolderCancellationRoute>()
                )
            }
        }
    }

    @Composable
    private fun Render() {
        NavHost(
            navController = controller,
            startDestination = "Unit test"
        ) {
            composable("Unit test") {
                Button(
                    onClick = {
                        controller.navigateToHolderUserCancellationDialog()
                    }
                ) {
                    Text("Navigate")
                }
            }

            configureHolderUserCancellationDialog(
                controller = controller,
                onCancelJourney = composeTestRule::incrementCancelJourneyClickCount
            )
        }
    }
}