package uk.gov.onelogin.sharing.testapp.destination

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.testapp.MainActivityRule
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination.Companion.configureTestAppRoutes

@RunWith(AndroidJUnit4::class)
class ModuleEntriesHolderTest {
    @get:Rule
    val composeTestRule = MainActivityRule(createComposeRule())

    private lateinit var controller: TestNavHostController

    private var capturedRoute: Any? = null

    @Test
    fun updatesRouteViaLambda() = runTest {
        composeTestRule.setContent {
            SetupNavHost()
        }

        composeTestRule.performMenuItemClick("Welcome screen")

        assertNotNull(capturedRoute)
    }

    @Composable
    private fun SetupNavHost() {
        controller = TestNavHostController(LocalContext.current)
        controller.navigatorProvider.addNavigator(ComposeNavigator())
        NavHost(
            navController = controller,
            startDestination = PrimaryTabDestination.Holder
        ) {
            configureTestAppRoutes { route, _ ->
                this@ModuleEntriesHolderTest.capturedRoute = route
            }
        }
    }
}
