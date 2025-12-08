@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.holder.HolderWelcomeScreenPermissionsStub.fakeDeniedPermissionsState
import uk.gov.onelogin.sharing.holder.HolderWelcomeScreenPermissionsStub.fakeGrantedPermissionsState
import uk.gov.onelogin.sharing.holder.presentation.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.holder.presentation.BluetoothState
import uk.gov.onelogin.sharing.holder.presentation.HolderScreenContent
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreen
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenPreview
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
import uk.gov.onelogin.sharing.holder.util.MainDispatcherRule
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.engagement.Engagement
import uk.gov.onelogin.sharing.security.engagement.FakeEngagementGenerator
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

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

    private val dummyEngagementData = "ENGAGEMENT_DATA"

    private fun createViewModel(
        mdocBleSession: FakeMdocSessionManager = FakeMdocSessionManager(),
        engagementGenerator: Engagement = FakeEngagementGenerator(data = dummyEngagementData),
        sessionSecurity: SessionSecurity = FakeSessionSecurity(publicKey = null)
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        sessionSecurity = sessionSecurity,
        engagementGenerator = engagementGenerator,
        mdocSessionManagerFactory = { mdocBleSession },
        dispatcher = mainDispatcherRule.testDispatcher
    )

    @Test
    fun `should show QR code content when permissions granted`() {
        composeTestRule.setContent {
            HolderScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = "Fake90109jec",
                    bluetoothState = BluetoothState.Enabled,
                    hasBluetoothPermissions = true
                )
            )
        }
        composeTestRule.assertWelcomeTextIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
    }

    @Test
    fun `should start bluetooth advertisement once granted permissions`() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            BluetoothPermissionPrompt(
                multiplePermissionsState = fakeGrantedPermissionsState,
                hasPreviouslyRequestedPermission = true,
                onGrantedPermissions = {
                    DisposableEffect(Unit) {
                        onDispose {
                            viewModel.stopAdvertising()
                        }
                    }
                }
            )
        }

        assertEquals(
            MdocSessionState.Idle,
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun `should stop bluetooth advertisement when composable leaves composition`() {
        val viewModel = createViewModel()

        var showContent by mutableStateOf(true)

        composeTestRule.setContent {
            if (showContent) {
                BluetoothPermissionPrompt(
                    multiplePermissionsState = fakeGrantedPermissionsState,
                    hasPreviouslyRequestedPermission = true,
                    onGrantedPermissions = {
                        DisposableEffect(Unit) {
                            onDispose {
                                viewModel.stopAdvertising()
                            }
                        }
                    }
                )
            }
        }

        composeTestRule.runOnUiThread {
            showContent = false
        }

        composeTestRule.waitForIdle()

        assertEquals(
            MdocSessionState.AdvertisingStopped,
            viewModel.uiState.value.sessionState
        )
    }

    @Test
    fun initiallyDisplaysEnablePermissionButtonBeforeRequestingPermissions() {
        composeTestRule.setContent {
            val permissionsState = fakeDeniedPermissionsState
            BluetoothPermissionPrompt(
                multiplePermissionsState = permissionsState,
                hasPreviouslyRequestedPermission = false
            ) {}
        }

        composeTestRule.assertEnablePermissionsButtonTextIsDisplayed()
    }

    @Test
    fun holderScreenSetsBluetoothStatusUnknownWhenPermissionsAreGranted() = runTest {
        val mdocBleSession = FakeMdocSessionManager().apply {
            mockBluetoothEnabled = false
        }
        val viewModel = createViewModel(mdocBleSession = mdocBleSession)

        composeTestRule.setContent {
            HolderWelcomeScreen(viewModel)
        }

        viewModel.updateBluetoothPermissions(true)

        composeTestRule.waitForIdle()
        assertEquals(
            BluetoothState.Unknown,
            viewModel.uiState.value.bluetoothState
        )
    }

    @Test
    fun `holder screen shows bluetooth disabled screen when bluetooth is disabled`() {
        composeTestRule.setContent {
            HolderScreenContent(
                contentState = HolderWelcomeUiState(
                    bluetoothState = BluetoothState.Disabled,
                    hasBluetoothPermissions = true
                )
            )
        }

        composeTestRule.assertBluetoothDisabledTextIsDisplayed()
    }

    @Test
    fun displaysErrorScreenWhenShowErrorScreenIsTrue() {
        composeTestRule.setContent {
            HolderScreenContent(
                contentState = HolderWelcomeUiState(
                    bluetoothState = BluetoothState.Disabled,
                    hasBluetoothPermissions = true,
                    showErrorScreen = true
                )
            )
        }
        composeTestRule.onNodeWithText("An error has occurred")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun displaysQrCode() = runTest {
        composeTestRule.apply {
            composeTestRule.viewModel.updateBluetoothPermissions(true)
            render()
            advanceUntilIdle()
        }
        composeTestRule.assertQrCodeIsDisplayed()
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            HolderWelcomeScreenPreview()
        }

        composeTestRule.assertQrCodeIsDisplayed()
    }
}
