@file:OptIn(ExperimentalPermissionsApi::class)

package uk.gov.onelogin.sharing.holder

import android.Manifest
import android.content.Context
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.bluetooth.api.AdvertiserState
import uk.gov.onelogin.sharing.bluetooth.api.BleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.ble.FakeBleAdvertiser
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.holder.EngagementGeneratorStub.Companion.BASE64_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.holder.HolderWelcomeScreenPermissionsStub.fakeGrantedPermissionsState
import uk.gov.onelogin.sharing.holder.engagement.Engagement
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenContent
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenPreview
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeViewModel
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
        bleAdvertiser: BleAdvertiser = FakeBleAdvertiser(),
        engagementGenerator: Engagement = FakeEngagementGenerator(data = dummyEngagementData),
        sessionSecurity: SessionSecurity = FakeSessionSecurity(publicKey = null)
    ): HolderWelcomeViewModel = HolderWelcomeViewModel(
        sessionSecurity = sessionSecurity,
        engagementGenerator = engagementGenerator,
        bleAdvertiser = bleAdvertiser,
        dispatcher = mainDispatcherRule.testDispatcher
    )

    @Test
    fun `should show QR code content when permissions granted`() {
        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = BASE64_ENCODED_DEVICE_ENGAGEMENT
                ),
                modifier = Modifier,
                multiplePermissionsState = fakeGrantedPermissionsState,
                hasPreviouslyRequestedPermission = true
            ) {}
        }
        composeTestRule.assertWelcomeTextIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
    }

    @Test
    fun `should start bluetooth advertisement once granted permissions`() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = BASE64_ENCODED_DEVICE_ENGAGEMENT
                ),
                modifier = Modifier,
                multiplePermissionsState = fakeGrantedPermissionsState,
                hasPreviouslyRequestedPermission = true
            ) {
                DisposableEffect(Unit) {
                    viewModel.startAdvertising()
                    onDispose {
                        viewModel.stopAdvertising()
                    }
                }
            }
        }

        assertEquals(AdvertiserState.Started, viewModel.uiState.value.advertiserState)
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            HolderWelcomeScreenPreview()
        }

        composeTestRule.onNodeWithContentDescription("QR Data")
    }
}
