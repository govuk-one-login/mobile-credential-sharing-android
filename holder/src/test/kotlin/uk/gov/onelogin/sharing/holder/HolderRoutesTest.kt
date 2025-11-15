package uk.gov.onelogin.sharing.holder

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.HolderRoutes.configureHolderRoutes
import uk.gov.onelogin.sharing.holder.presentation.HolderHomeRoute
import uk.gov.onelogin.sharing.verifier.HolderWelcomeScreenRule

@RunWith(AndroidJUnit4::class)
class HolderRoutesTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(
        composeTestRule = createComposeRule()
    )

    @Test
    fun holderRoutesAreConfigured() {
        composeTestRule.setContent {
            val controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = HolderHomeRoute
            ) {
                configureHolderRoutes()
            }
        }

        composeTestRule.assertWelcomeTextIsDisplayed()
    }
}
