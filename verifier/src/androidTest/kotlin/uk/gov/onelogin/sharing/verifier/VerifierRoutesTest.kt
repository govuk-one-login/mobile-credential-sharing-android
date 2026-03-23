package uk.gov.onelogin.sharing.verifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult
import uk.gov.onelogin.sharing.verifier.verify.VerifyCredentialRoute

@RunWith(AndroidJUnit4::class)
class VerifierRoutesTest {
    @get:Rule
    val composeTestRule = VerifierScannerRule(
        composeTestRule = createComposeRule()
    )

    private lateinit var controller: TestNavHostController

    @Test
    fun verifierScanRouteIsStartingRouteOfNestedNavigationGraph() = runTest {
        composeTestRule.setContent {
            SetupNavHost()
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<VerifierScanRoute>()

        assertNotNull(route)
    }

    @Test
    fun invalidQrNavigatesBackToScannerWhenTryingAgain() = runTest {
        composeTestRule.setContent {
            SetupNavHost {
                controller.navigate(ScannedInvalidQrRoute(invalidBarcodeDataResultOne.data))
            }
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<VerifierScanRoute>()

        assertNotNull(route)
    }

    @Test
    fun validQrCodesNavigateToConnectingWithHolder() = runTest {
        composeTestRule.setContent {
            SetupNavHost {
                controller.navigate(ConnectWithHolderDeviceRoute)
            }
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<ConnectWithHolderDeviceRoute>()

        assertNotNull(route)
    }

    @Composable
    private fun SetupNavHost(postConfiguration: @Composable () -> Unit = {}) {
        val context = LocalContext.current
        controller = TestNavHostController(context)
        controller.navigatorProvider.addNavigator(ComposeNavigator())

        NavHost(
            navController = controller,
            startDestination = VerifierRoutes
        ) {
            navigation<VerifierRoutes>(startDestination = VerifyCredentialRoute) {
                composable<VerifyCredentialRoute> {}
                composable<VerifierScanRoute> {}
                composable<ScannedInvalidQrRoute> {}
                composable<ConnectWithHolderDeviceRoute> {}
                composable<BluetoothConnectionErrorRoute> {}
            }
        }
        postConfiguration()
    }
}
