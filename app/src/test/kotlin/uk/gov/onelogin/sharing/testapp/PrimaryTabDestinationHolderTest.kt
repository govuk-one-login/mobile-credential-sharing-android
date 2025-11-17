package uk.gov.onelogin.sharing.testapp

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.PrimaryTabDestination.Companion.configureTestAppRoutes

@RunWith(AndroidJUnit4::class)
class PrimaryTabDestinationHolderTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var controller: TestNavHostController

    private var route: Any? = null

    @Test
    fun updatesRouteViaLambda() = runTest {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())
            NavHost(
                navController = controller,
                startDestination = PrimaryTabDestination.Holder
            ) {
                configureTestAppRoutes { route, _ ->
                    this@PrimaryTabDestinationHolderTest.route = route
                }
            }
        }

        composeTestRule.onNodeWithText("Welcome screen").performClick()

        assertNotNull(route)
    }
}
