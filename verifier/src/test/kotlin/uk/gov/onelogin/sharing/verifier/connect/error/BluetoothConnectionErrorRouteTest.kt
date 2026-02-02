package uk.gov.onelogin.sharing.verifier.connect.error

import CredentialSharingAppGraphStub
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.configureBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.connect.error.BluetoothConnectionErrorRoute.Companion.navigateToBluetoothConnectionErrorRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

@RunWith(AndroidJUnit4::class)
class BluetoothConnectionErrorRouteTest {
    @get:Rule
    val composeTestRule = BluetoothConnectionErrorScreenRule(createComposeRule())

    lateinit var controller: TestNavHostController

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun verifyControllerNavigationExtensionFunction() = runTest {
        val appGraph = CredentialSharingAppGraphStub(
            applicationContext = ApplicationProvider.getApplicationContext()
        )

        composeTestRule.run {
            setContent {
                val context = LocalContext.current
                controller = TestNavHostController(context)
                controller.navigatorProvider.addNavigator(ComposeNavigator())

                NavHost(
                    navController = controller,
                    startDestination = ConnectWithHolderDeviceRoute(
                        validBarcodeDataResult.data
                    )
                ) {
                    configureConnectWithHolderDeviceRoute(appGraph = appGraph)
                    configureBluetoothConnectionErrorRoute(controller = controller)
                }
            }

            controller.navigateToBluetoothConnectionErrorRoute("This is a unit test!")

            performTryAgainButtonClick()

            val route = controller.currentBackStackEntry?.toRoute<ConnectWithHolderDeviceRoute>()

            assertNotNull(route)
        }
    }
}
