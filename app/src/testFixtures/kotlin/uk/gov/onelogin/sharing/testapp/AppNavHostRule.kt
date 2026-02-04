package uk.gov.onelogin.sharing.testapp

import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.core.app.ApplicationProvider
import kotlin.reflect.KClass
import org.junit.Assert.assertNotNull
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph

class AppNavHostRule(
    private val composeTestRule: ComposeContentTestRule,
    private val appGraph: CredentialSharingAppGraph
) : ComposeContentTestRule by composeTestRule {
    private lateinit var controller: TestNavHostController
    private val context: Context = ApplicationProvider.getApplicationContext()

    fun <T : Any> assertCurrentRoute(klass: KClass<T>): T {
        val result = controller.currentBackStackEntry?.toRoute<T>(klass)
        assertNotNull(result)

        return result!!
    }

    fun navigate(route: Any) = controller.navigate(route)

    fun renderWithController(startDestination: Any, modifier: Modifier = Modifier) {
        setContent {
            controller = TestNavHostController(context)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            AppNavHost(
                appGraph = appGraph,
                modifier = modifier.testTag("appNavHost"),
                navController = controller,
                startDestination = startDestination
            )
        }
    }

    fun renderWithoutController(startDestination: Any, modifier: Modifier = Modifier) {
        setContent {
            AppNavHost(
                appGraph = appGraph,
                modifier = modifier.testTag("appNavHost"),
                startDestination = startDestination
            )
        }
    }
}
