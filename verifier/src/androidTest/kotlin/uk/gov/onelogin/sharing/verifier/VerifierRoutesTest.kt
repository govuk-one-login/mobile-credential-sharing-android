package uk.gov.onelogin.sharing.verifier

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule

@RunWith(AndroidJUnit4::class)
class VerifierRoutesTest {

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        composeTestRule = createComposeRule()
    )

    @Test
    fun verifierRoutesAreConfigured() {
        composeTestRule.setContent {
            val controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = VerifierRoutes
            ) {
                configureVerifierRoutes()
            }
        }

        composeTestRule.assertPermissionDeniedButtonIsDisplayed()
    }
}
