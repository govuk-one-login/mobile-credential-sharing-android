@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder

import android.content.Context
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.BluetoothStatus
import uk.gov.onelogin.sharing.bluetooth.api.FakeMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.api.MdocSessionState
import uk.gov.onelogin.sharing.holder.HolderWelcomeScreenPermissionsStub.fakeGrantedPermissionsState
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.holder.presentation.BluetoothPermissionPrompt
import uk.gov.onelogin.sharing.holder.presentation.BluetoothState
import uk.gov.onelogin.sharing.holder.presentation.HolderScreenContent
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
import uk.gov.onelogin.sharing.holder.presentation.QrContent
import uk.gov.onelogin.sharing.holder.util.MainDispatcherRule
import uk.gov.onelogin.sharing.security.FakeSessionSecurity
import uk.gov.onelogin.sharing.security.secureArea.SessionSecurity

@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule =
        HolderWelcomeScreenRule(composeTestRule = createComposeRule(), resources = resources)

    private val dummyEngagementData = "ENGAGEMENT_DATA"

    private fun createViewModel(
        mdocSessionManager: FakeMdocSessionManager = FakeMdocSessionManager(),
        engagementGenerator: Engagement = FakeEngagementGenerator(data = dummyEngagementData),
        sessionSecurity: SessionSecurity = FakeSessionSecurity(publicKey = null)
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        sessionSecurity = sessionSecurity,
        engagementGenerator = engagementGenerator,
        mdocBleSession = mdocSessionManager,
        dispatcher = mainDispatcherRule.testDispatcher
    )

    @Test
    fun `should show QR code content when permissions granted`() {
        composeTestRule.setContent {
            HolderScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = "Fake90109jec",
                    bluetoothStatus = BluetoothState.Enabled,
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
                        viewModel.startAdvertising()
                        onDispose {
                            viewModel.stopAdvertising()
                        }
                    }
                }
            )
        }

        assertEquals(MdocSessionState.Started, viewModel.uiState.value.sessionState)
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
                            viewModel.startAdvertising()
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

        assertEquals(MdocSessionState.Stopped, viewModel.uiState.value.sessionState)
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            QrContent(
                contentState = HolderWelcomeUiState(qrData = "192301092doi"),
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithContentDescription("QR Data")
    }
}
