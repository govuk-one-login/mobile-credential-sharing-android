package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

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
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
class ScannedInvalidQrRouteTest {

    @get:Rule
    val composeTestRule = ScannedInvalidQrScreenRule(createComposeRule())

    lateinit var controller: TestNavHostController

    private var hasClickedOnTryAgain = false

    @Test
    fun verifyNavGraphEntry() = runTest {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ScannedInvalidQrRoute(invalidBarcodeDataResultOne.data)
            ) {
                configureScannedInvalidQrRoute(
                    onTryAgainClick = { hasClickedOnTryAgain = true }
                )
            }
        }

        composeTestRule.performTryAgainButtonClick()

        testScheduler.advanceUntilIdle()

        assertTrue(hasClickedOnTryAgain)
    }

    @Test
    fun verifyControllerNavigationExtensionFunction() = runTest {
        composeTestRule.setContent {
            val context = LocalContext.current
            controller = TestNavHostController(context)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ConnectWithHolderDeviceRoute(validBarcodeDataResult.data)
            ) {
                configureConnectWithHolderDeviceRoute(context)
                configureScannedInvalidQrRoute()
            }

            controller.navigateToScannedInvalidQrRoute(invalidBarcodeDataResultOne.data)
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<ScannedInvalidQrRoute>()

        assertNotNull(route)
    }
}
