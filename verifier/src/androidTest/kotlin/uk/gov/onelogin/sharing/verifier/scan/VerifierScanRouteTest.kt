package uk.gov.onelogin.sharing.verifier.scan

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne

@RunWith(AndroidJUnit4::class)
class VerifierScanRouteTest {

    @get:Rule
    val composeTestRule = VerifierScannerRule(createComposeRule())

    lateinit var controller: TestNavHostController

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun verifyNavGraphEntry() = runTest {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = VerifierScanRoute
            ) {
                configureVerifierScannerRoute(context = context)
            }
        }

        composeTestRule.assertPermissionDeniedButtonIsDisplayed()
    }

    @Test
    fun verifyControllerNavigationExtensionFunction() = runTest {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ScannedInvalidQrRoute(invalidBarcodeDataResultOne.data)
            ) {
                configureVerifierScannerRoute(context = context)
                configureScannedInvalidQrRoute()
            }

            controller.navigateToVerifierScanRoute()
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<VerifierScanRoute>()

        assertNotNull(route)
    }
}
