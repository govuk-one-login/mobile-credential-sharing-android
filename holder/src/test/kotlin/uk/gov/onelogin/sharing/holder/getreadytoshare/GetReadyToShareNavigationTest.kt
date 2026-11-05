package uk.gov.onelogin.sharing.holder.getreadytoshare

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.DialogNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.android.ui.componentsv2.rules.ComposeContentTestRuleExtensions.onNodeWithRole
import uk.gov.onelogin.sharing.holder.getreadytoshare.GetReadyToShareNavigationExt.configureGetReadyToShare
import uk.gov.onelogin.sharing.holder.getreadytoshare.GetReadyToShareNavigationExt.navigateToGetReadyToShare

@RunWith(AndroidJUnit4::class)
class GetReadyToShareNavigationTest {

    @get:Rule
    val composeTestRule = GetReadyToShareScreenRule()

    private lateinit var controller: TestNavHostController

    @Test
    fun `Can navigate to get ready to share screen`() = runTest {
        composeTestRule.run {
            setContent {
                controller = prepareController(LocalContext.current)

                Render()
            }

            onNodeWithRole(Role.Button)
                .performClick()

            waitUntil {
                controller.currentBackStackEntry?.toRoute<GetReadyToShareRoute>() != null
            }
        }
    }

    private fun prepareController(context: Context): TestNavHostController =
        TestNavHostController(context).apply {
            navigatorProvider.addNavigator(ComposeNavigator())
            navigatorProvider.addNavigator(DialogNavigator())
        }

    @Test
    fun `Navigates back a screen by default`() = runTest {
    }

    @Composable
    private fun Render() {
        NavHost(
            navController = controller,
            startDestination = "entry"
        ) {
            composable("entry") {
                Button(
                    onClick = {
                        controller.navigateToGetReadyToShare()
                    }
                ) {
                    Text("This is a unit test")
                }
            }
            configureGetReadyToShare()
        }
    }
}
