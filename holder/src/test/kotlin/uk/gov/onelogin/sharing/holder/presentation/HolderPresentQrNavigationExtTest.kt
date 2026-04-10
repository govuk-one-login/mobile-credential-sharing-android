package uk.gov.onelogin.sharing.holder.presentation

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.holder.HolderWelcomeScreenRule
import uk.gov.onelogin.sharing.holder.consent.HolderConsentRoute
import uk.gov.onelogin.sharing.holder.presentation.HolderPresentQrNavigationExt.navigateToHolderPresentQrScreen

@RunWith(AndroidJUnit4::class)
class HolderPresentQrNavigationExtTest {

    @get:Rule
    val composeTestRule = HolderWelcomeScreenRule(createComposeRule())

    private lateinit var controller: TestNavHostController

    @Test
    fun `navigateToHolderPresentQrScreen navigates to HolderPresentQrRoute`() = runTest {
        composeTestRule.setContent {
            val context = LocalContext.current
            controller = TestNavHostController(context).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = controller, startDestination = HolderConsentRoute) {
                composable<HolderConsentRoute> {}
                composable<HolderPresentQrRoute> {}
            }
            controller.navigateToHolderPresentQrScreen()
        }

        assertNotNull(controller.currentBackStackEntry?.toRoute<HolderPresentQrRoute>())
    }
}
