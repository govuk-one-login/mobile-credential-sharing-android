package uk.gov.onelogin.sharing.verifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.VerifierRoutes.configureVerifierRoutes
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScanRoute
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerRule
import uk.gov.onelogin.sharing.verifier.scan.VerifierScannerViewModel
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrScreenRule
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
class VerifierRoutesTest {

    @get:Rule
    val composeTestRule = VerifierScannerRule(
        composeTestRule = createComposeRule()
    )

    private val scannedInvalidQrScreenRule = ScannedInvalidQrScreenRule(
        composeContentTestRule = composeTestRule
    )

    private lateinit var controller: TestNavHostController

    @Test
    fun verifierScanRouteIsStartingRouteOfNestedNavigationGraph() = runTest {
        composeTestRule.setContent {
            SetupNavHost()
        }

        composeTestRule.assertPermissionDeniedButtonIsDisplayed()
    }

    @Test
    fun invalidQrNavigatesBackToScannerWhenTryingAgain() = runTest {
        composeTestRule.setContent {
            SetupNavHost {
                controller.navigate(ScannedInvalidQrRoute(invalidBarcodeDataResultOne.data))
            }
        }

        testScheduler.advanceUntilIdle()

        scannedInvalidQrScreenRule.assertTitleIsDisplayed()
        scannedInvalidQrScreenRule.performTryAgainButtonClick()

        val route = controller.currentBackStackEntry?.toRoute<VerifierScanRoute>()

        assertNotNull(route)
    }

    @Test
    fun validQrCodesNavigateToConnectingWithHolder() = runTest {
        composeTestRule.setContent {
            SetupNavHost {
                controller.navigate(ConnectWithHolderDeviceRoute(validBarcodeDataResult.data))
            }
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<ConnectWithHolderDeviceRoute>()

        assertNotNull(route)
    }

    @Composable
    private fun SetupNavHost(postConfiguration: @Composable () -> Unit = {}) {
        controller = TestNavHostController(LocalContext.current)
        controller.navigatorProvider.addNavigator(ComposeNavigator())

        NavHost(
            navController = controller,
            startDestination = VerifierRoutes
        ) {
            configureVerifierRoutes(navController = controller)
        }
        postConfiguration()
    }
}
