package uk.gov.onelogin.sharing.testapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import kotlin.reflect.KClass
import org.junit.Assert.assertNotNull

class AppNavHostRule(private val composeTestRule: ComposeContentTestRule) :
    ComposeContentTestRule by composeTestRule {
    private lateinit var controller: TestNavHostController

    fun <T : Any> assertCurrentRoute(klass: KClass<T>): T {
        val result = controller.currentBackStackEntry?.toRoute<T>(klass)
        assertNotNull(result)

        return result!!
    }

    fun navigate(route: Any) = controller.navigate(route)

    fun renderWithController(startDestination: Any, modifier: Modifier = Modifier) {
        setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            AppNavHost(
                modifier = modifier.testTag("appNavHost"),
                navController = controller,
                startDestination = startDestination
            )
        }
    }

    fun renderWithoutController(startDestination: Any, modifier: Modifier = Modifier) {
        setContent {
            AppNavHost(
                modifier = modifier.testTag("appNavHost"),
                startDestination = startDestination
            )
        }
    }
}
