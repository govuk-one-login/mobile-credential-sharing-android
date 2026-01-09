package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
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
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateDenied
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceStateStubs.fakePermissionStateGranted

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifyCredentialScreenTest {
    @get:Rule
    val composeTestRule = VerifyCredentialRule(createComposeRule())

    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()

    private lateinit var viewModel: VerifyCredentialViewModel

    @Before
    fun setup() {
        Intents.init()
        viewModel = VerifyCredentialViewModel(
            logger,
            bluetoothStateMonitor
        )
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `bluetooth system prompt is displayed when state is bluetooth disabled`() = runTest {
        composeTestRule.setContent {
            VerifyCredentialScreen(
                viewModel = viewModel,
                multiplePermissionsState = fakePermissionStateGranted
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.OFF)
        composeTestRule.waitForIdle()

        composeTestRule.assertBluetoothPromptIsDisplayed()
    }

    @Test
    fun `bluetooth permission prompt is displayed when permissions are denied`() {
        composeTestRule.setContent {
            VerifyCredentialScreen(
                viewModel = viewModel,
                multiplePermissionsState = fakePermissionStateDenied
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Enable bluetooth permissions")
            .assertIsDisplayed()
    }

    @Test
    fun `navigates when prerequisites are met`() = runTest {
        var navigated = false

        composeTestRule.setContent {
            VerifyCredentialScreen(
                viewModel = viewModel,
                multiplePermissionsState = fakePermissionStateGranted,
                navigateToScanner = { navigated = true }
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        composeTestRule.waitUntil { navigated }
        assertTrue(navigated)
    }

    @Test
    fun `onPermissionRequestLaunched is called when permissions request is launched`() {
        var launched = false

        val fakeDenied = FakeMultiplePermissionsState(
            permissions = fakePermissionStateDenied.permissions,
            onLaunchPermission = {
                launched = true
                viewModel.onPermissionRequestLaunched()
            }
        )

        composeTestRule.setContent {
            VerifyCredentialScreen(
                viewModel = viewModel,
                multiplePermissionsState = fakeDenied
            )
        }

        composeTestRule.waitUntil { launched }

        assertTrue(viewModel.uiState.value.hasPreviouslyRequestedPermission)
    }
}
