package uk.gov.onelogin.sharing.verifier.scan

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.configureVerifierScannerRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute.navigateToVerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrScreenRule
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
class VerifierScanRouteTest {

    @get:Rule
    val composeTestRule = VerifierScannerRule(createComposeRule())

    lateinit var controller: TestNavHostController

    @Test
    fun verifyNavGraphEntry() = runTest {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = VerifierScanRoute
            ) {
                configureVerifierScannerRoute()
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
                configureVerifierScannerRoute()
                configureScannedInvalidQrRoute()
            }

            controller.navigateToVerifierScanRoute()
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<VerifierScanRoute>()

        assertNotNull(route)
    }
}
