package uk.gov.onelogin.sharing.verifier.verify

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlin.test.Test
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBluetoothStateMonitor
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator
import uk.gov.onelogin.sharing.orchestration.verifier.session.VerifierSessionState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class VerifyCredentialScreenTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = VerifyCredentialRule(createComposeRule())

    private val bluetoothStateMonitor = FakeBluetoothStateMonitor()
    private val logger = SystemLogger()

    private var initialSessionState: VerifierSessionState = VerifierSessionState.NotStarted

    private val orchestrator by lazy {
        FakeOrchestrator(
            initialVerifierState = MutableStateFlow(initialSessionState)
        )
    }
    private val viewModel by lazy {
        VerifierPrerequisitesViewModel(
            logger,
            bluetoothStateMonitor,
            orchestrator,
            dispatcherRule.testDispatcher
        )
    }

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun `bluetooth system prompt is displayed when state is bluetooth disabled`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.setContent {
            VerifierPrerequisitesScreen(
                viewModel = viewModel,
                multiplePermissionsState = bluetoothPermissionsGranted
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.OFF)
        composeTestRule.waitForIdle()

        composeTestRule.assertBluetoothPromptIsDisplayed()
    }

    @Test
    fun `bluetooth permission prompt is displayed when permissions are denied`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        composeTestRule.setContent {
            VerifierPrerequisitesScreen(
                viewModel = viewModel,
                multiplePermissionsState = bluetoothPermissionsDenied
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Enable bluetooth permissions")
            .assertIsDisplayed()
    }

    @Test
    fun `navigates when prerequisites are met`() = runTest(dispatcherRule.testDispatcher) {
        var navigated = false
        initialSessionState = VerifierSessionState.ReadyToScan

        composeTestRule.setContent {
            VerifierPrerequisitesScreen(
                viewModel = viewModel,
                multiplePermissionsState = bluetoothPermissionsGranted,
                onNavigateToScanner = { navigated = true }
            )
        }

        bluetoothStateMonitor.emit(BluetoothStatus.ON)

        composeTestRule.waitUntil { navigated }
        assertTrue(navigated)
    }

    @Test
    fun `onPermissionRequestLaunched is called when permissions request is launched`() = runTest(
        dispatcherRule.testDispatcher
    ) {
        var launched = false

        val fakeDenied = FakeMultiplePermissionsState(
            permissions = bluetoothPermissionsDenied.permissions,
            onLaunchPermission = {
                launched = true
                viewModel.onPermissionRequestLaunched()
            }
        )

        composeTestRule.setContent {
            VerifierPrerequisitesScreen(
                viewModel = viewModel,
                multiplePermissionsState = fakeDenied
            )
        }

        composeTestRule.waitUntil { launched }

        assertTrue(viewModel.uiState.value.hasPreviouslyRequestedPermission)
    }
}
