package uk.gov.onelogin.sharing.verifier.connect

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.configureConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceRoute.Companion.navigateToConnectWithHolderDeviceRoute
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.validWithCorrectBluetoothSetup
import uk.gov.onelogin.sharing.verifier.rules.ShadowLogFile
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.errors.invalid.ScannedInvalidQrRoute.Companion.configureScannedInvalidQrRoute
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs

@RunWith(AndroidJUnit4::class)
@Config(
    shadows = [ShadowLog::class]
)
class ConnectWithHolderDeviceRouteTest {

    @get:Rule
    val loggingFile = ShadowLogFile(fileName = this::class.java.simpleName)

    @get:Rule
    val composeTestRule = ConnectWithHolderDeviceRule(createComposeRule())

    lateinit var controller: TestNavHostController

    @Test
    fun verifyControllerNavigationExtensionFunction() = runTest {
        composeTestRule.setContent {
            val context = LocalContext.current
            controller = TestNavHostController(context)
            controller.navigatorProvider.addNavigator(ComposeNavigator())

            NavHost(
                navController = controller,
                startDestination = ConnectWithHolderDeviceRoute(
                    validWithCorrectBluetoothSetup.base64EncodedEngagement!!
                )
            ) {
                configureConnectWithHolderDeviceRoute(context)
                configureScannedInvalidQrRoute()
            }
        }

        val route = controller.currentBackStackEntry?.toRoute<ConnectWithHolderDeviceRoute>()

        assertNotNull(route)
    }
}
