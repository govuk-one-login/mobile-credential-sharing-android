package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute

@RunWith(AndroidJUnit4::class)
class HolderRoutesTest {

    lateinit var controller: NavHostController

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(
        composeTestRule = createComposeRule()
    )

    @Test
    fun holderRoutesAreConfigured() = runTest {
        composeTestRule.setContent {
            val context = LocalContext.current
            controller = TestNavHostController(context)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = HolderHomeRoute
            ) {
                composable<HolderHomeRoute> {}
            }
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<HolderHomeRoute>()

        assertNotNull(route)
    }
}
