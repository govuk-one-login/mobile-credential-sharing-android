package uk.gov.onelogin.sharing.verifier.verify

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.shouldShowRationale
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
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

    @Test
    fun `onPermissionRequestLaunched is called when permissions request is launched`() {
        val fakeDenied = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    PermissionStatus.Denied(false)
                ),
                FakePermissionState(
                    Manifest.permission.BLUETOOTH_SCAN,
                    PermissionStatus.Denied(false)
                ),
                FakePermissionState(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    PermissionStatus.Denied(false)
                )
            ),
            onLaunchPermission = { viewModel.onPermissionRequestLaunched() }
        )

        composeTestRule.setContent {
            VerifyCredential(
                viewModel = viewModel,
                multiplePermissionsState = fakeDenied,
                scannerContent = { Box(Modifier.testTag("fakeScanner")) }
            )
        }

        composeTestRule.onNodeWithText("Enable bluetooth permissions").performClick()
        composeTestRule.waitForIdle()

        assertTrue(viewModel.uiState.value.hasPreviouslyRequestedPermission)
    }
}
