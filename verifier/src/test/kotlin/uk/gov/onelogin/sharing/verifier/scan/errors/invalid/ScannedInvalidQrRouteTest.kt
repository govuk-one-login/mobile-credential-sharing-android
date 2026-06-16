package uk.gov.onelogin.sharing.verifier.scan.errors.invalid

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.navigateToScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.invalidBarcodeDataResultOne
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesNavigationExt.configureVerifierPrerequisitesRoute
import uk.gov.onelogin.sharing.verifier.verify.VerifierPrerequisitesRoute

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ScannedInvalidQrRouteTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = ScannedInvalidQrScreenRule(createComposeRule())

    lateinit var controller: TestNavHostController

    @Test
    fun verifyNavGraphEntry() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ScannedInvalidQrRoute(invalidBarcodeDataResultOne)
            ) {
                configureVerifierPrerequisitesRoute()
                configureScannedInvalidQrRoute(controller)
            }
        }

        composeTestRule.performTryAgainButtonClick()
        advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<VerifierPrerequisitesRoute>()

        assertNotNull(route)
    }

    @Test
    fun verifyControllerNavigationExtensionFunction() = runTest(dispatcherRule.testDispatcher) {
        composeTestRule.setContent {
            val context = LocalContext.current
            controller = TestNavHostController(context)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ConnectWithHolderDeviceRoute
            ) {
                composable<ConnectWithHolderDeviceRoute> {}
                composable<ScannedInvalidQrRoute> {}
            }

            controller.navigateToScannedInvalidQrRoute(invalidBarcodeDataResultOne)
        }

        testScheduler.advanceUntilIdle()

        val route = controller.currentBackStackEntry?.toRoute<ScannedInvalidQrRoute>()

        assertNotNull(route)
    }
}
