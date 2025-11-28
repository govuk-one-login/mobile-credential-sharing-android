package uk.gov.onelogin.sharing.testapp.destination

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import com.google.testing.junit.testparameterinjector.TestParameter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestParameterInjector
import uk.gov.onelogin.sharing.testapp.MainActivityRule
import uk.gov.onelogin.sharing.testapp.destination.PrimaryTabDestination.Companion.configureTestAppRoutes

@RunWith(RobolectricTestParameterInjector::class)
class ModuleEntriesVerifierTest {
    @get:Rule
    val composeTestRule = MainActivityRule(createComposeRule())

    private lateinit var controller: TestNavHostController

    private var capturedRoute: Any? = null

    @Test
    fun updatesRouteViaLambda(
        @TestParameter(valuesProvider = VerifierModuleEntriesProvider::class) menuItem: String
    ) = runTest {
        composeTestRule.setContent {
            SetupNavHost()
        }

        composeTestRule.performMenuItemClick(menuItem)

        assertNotNull(capturedRoute)
    }

    @Composable
    private fun SetupNavHost() {
        controller = TestNavHostController(LocalContext.current)
        controller.navigatorProvider.addNavigator(ComposeNavigator())
        NavHost(
            navController = controller,
            startDestination = PrimaryTabDestination.Verifier
        ) {
            configureTestAppRoutes { route, _ ->
                capturedRoute = route
            }
        }
    }
}
