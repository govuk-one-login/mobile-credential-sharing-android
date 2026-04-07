@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.logging.testdouble.v2.SystemLogger
import uk.gov.onelogin.sharing.core.MainDispatcherRule
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsDenied
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsStateStubs.bluetoothPermissionsGranted
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPrompt
import uk.gov.onelogin.sharing.core.presentation.permissions.PermissionPromptText
import uk.gov.onelogin.sharing.holder.R
import uk.gov.onelogin.sharing.holder.presentation.HolderScreenContent
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenPreview
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
import uk.gov.onelogin.sharing.orchestration.FakeOrchestrator

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule =
        HolderWelcomeScreenRule(
            composeTestRule = createComposeRule(),
            resources = resources
        )

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun createViewModel(): HolderWelcomeViewModel = HolderWelcomeViewModel(
        dispatcher = mainDispatcherRule.testDispatcher,
        logger = SystemLogger(),
        savedStateHandle = SavedStateHandle(),
        orchestrator = FakeOrchestrator()
    )

    @Test
    fun `should show QR code content when permissions granted`() {
        composeTestRule.setContent {
            HolderScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = "Fake90109jec",
                    hasBluetoothPermissions = true
                ),
                multiplePermissionsState = bluetoothPermissionsGranted,
                hasPreviouslyRequestedPermission = true,
                grantedAllPerms = {}
            )
        }
        composeTestRule.assertWelcomeTextIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
    }

    @Test
    fun initiallyDisplaysEnablePermissionButtonBeforeRequestingPermissions() {
        composeTestRule.setContent {
            val permissionsState = bluetoothPermissionsDenied
            PermissionPrompt(
                multiplePermissionsState = permissionsState,
                hasPreviouslyRequestedPermission = false,
                text = PermissionPromptText(
                    permanentlyDeniedText = resources.getString(
                        R.string.bluetooth_permission_permanently_denied
                    ),
                    enablePermissionText = resources.getString(
                        R.string.enable_bluetooth_permission
                    ),
                    openSettingsText = resources.getString(R.string.open_app_permissions),
                    deniedText = resources.getString(R.string.bluetooth_permission_denied)
                )
            ) {}
        }

        composeTestRule.assertEnablePermissionsButtonTextIsDisplayed()
    }

    @Test
    fun holderScreenSetsBluetoothStatusUnknownWhenPermissionsAreGranted() = runTest {
        val viewModel = createViewModel()

        composeTestRule.setContent {
            HolderWelcomeScreen(viewModel)
        }

        viewModel.updateBluetoothPermissions(true)

        composeTestRule.waitForIdle()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun displaysQrCode() = runTest {
        composeTestRule.apply {
            composeTestRule.viewModel.updateBluetoothPermissions(true)
            render(
                state = HolderWelcomeUiState(
                    qrData = "fakestring",
                    hasBluetoothPermissions = true
                )
            )
            advanceUntilIdle()
        }
        composeTestRule.assertQrCodeIsDisplayed()
    }

    @Test
    fun showsErrorScreenWhenContentStateShowErrorScreenIsTrue() = runTest {
        composeTestRule.apply {
            render(
                HolderWelcomeUiState(
                    showErrorScreen = true,
                    errorMessage = "Bluetooth permissions were revoked during the session",
                    hasBluetoothPermissions = true
                )
            )
        }

        advanceUntilIdle()

        composeTestRule.onNodeWithText("Bluetooth permissions were revoked during the session")
            .assertIsDisplayed()
    }

    @Test
    fun shouldNotShowEnableBluetoothPromptIfHasBluetoothPermissionsIsFalse() = runTest {
        composeTestRule.apply {
            render(
                HolderWelcomeUiState(
                    hasBluetoothPermissions = false
                )
            )
        }

        advanceUntilIdle()

        composeTestRule.onNodeWithTag(
            "EnableBluetoothPrompt"
        ).assertDoesNotExist()
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            HolderWelcomeScreenPreview()
        }

        composeTestRule.assertQrCodeIsDisplayed()
    }
}
