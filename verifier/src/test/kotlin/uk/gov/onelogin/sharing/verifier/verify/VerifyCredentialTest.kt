package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateDenied
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateGranted

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifyCredentialTest {
    @get:Rule
    val composeTestRule = VerifyCredentialRule(createComposeRule())

    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()

    private val viewModel = VerifyCredentialViewModel(
        logger,
        bluetoothStateMonitor
    )

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `bluetooth system prompt is displayed when state is bluetooth disabled`() = runTest {
        composeTestRule.setContent {
            VerifyCredential(
                viewModel = viewModel
            )
        }

        viewModel.onPermissionsChanged(true)
        bluetoothStateMonitor.emit(BluetoothStatus.OFF)
        composeTestRule.waitForIdle()

        composeTestRule.assertBluetoothPromptIsDisplayed()
    }

    @Test
    fun `scanner is displayed when prerequisites are met`() = runTest {
        val fakeScannerTag = "fakeScanner"

        composeTestRule.setContent {
            VerifyCredential(
                viewModel = viewModel,
                scannerContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag(fakeScannerTag)
                    )
                }
            )
        }

        viewModel.onPermissionsChanged(true)
        bluetoothStateMonitor.emit(BluetoothStatus.ON)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(fakeScannerTag)
            .assertIsDisplayed()
    }

    @Test
    fun `bluetooth permission prompt is displayed when permissions are denied`() {
        val fakeScannerTag = "fakeScanner"
        composeTestRule.setContent {
            VerifyCredential(
                viewModel = viewModel,
                multiplePermissionsState = fakePermissionStateDenied,
                scannerContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag(fakeScannerTag)
                    )
                }
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Enable bluetooth permissions")
            .assertIsDisplayed()

        composeTestRule.onNodeWithTag("fakeScanner").assertDoesNotExist()
    }

    @Test
    fun `bluetooth permission prompt is not displayed when permissions are granted`() {
        val fakeScannerTag = "fakeScanner"
        composeTestRule.setContent {
            VerifyCredential(
                viewModel = viewModel,
                multiplePermissionsState = fakePermissionStateGranted,
                scannerContent = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .testTag(fakeScannerTag)
                    )
                }
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.ON)
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Enable bluetooth permissions")
            .assertIsNotDisplayed()

        composeTestRule.onNodeWithTag("fakeScanner").assertIsDisplayed()
    }
}
