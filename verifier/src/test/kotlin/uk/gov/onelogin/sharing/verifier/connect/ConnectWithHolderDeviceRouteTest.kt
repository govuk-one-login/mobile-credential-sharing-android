package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.navigateToConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
class ConnectWithHolderDeviceRouteTest {

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    lateinit var controller: TestNavHostController

    @Test
    fun verifyControllerNavigationExtensionFunction() = runTest {
        composeTestRule.setContent {
            controller = TestNavHostController(LocalContext.current)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ScannedInvalidQrRoute(
                    BarcodeDataResultStubs.invalidBarcodeDataResultOne.data
                )
            ) {
                configureConnectWithHolderDeviceRoute()
                configureScannedInvalidQrRoute()
            }

            controller.navigateToConnectWithHolderDeviceRoute(validBarcodeDataResult.data)
        }

        composeTestRule.assertBasicInformationIsDisplayed(validBarcodeDataResult.data)
    }
}
