package uk.gov.onelogin.sharing.holder

import android.Manifest
import android.content.Context
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.holder.EngagementGeneratorStub.Companion.BASE64_ENCODED_DEVICE_ENGAGEMENT
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenContent
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeScreenPreview
import uk.gov.onelogin.sharing.holder.presentation.HolderWelcomeUiState

@RunWith(AndroidJUnit4::class)
class HolderWelcomeScreenTest {

    private val resources = ApplicationProvider.getApplicationContext<Context>().resources

    @get:Rule
    val composeTestRule =
        HolderWelcomeScreenRule(composeTestRule = createComposeRule(), resources = resources)

    @OptIn(ExperimentalPermissionsApi::class)
    @Test
    fun `should show QR code content when permissions granted`() {
        val fakeState = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Granted
                )
            ),
            onLaunchPermission = { }
        )

        composeTestRule.setContent {
            HolderWelcomeScreenContent(
                contentState = HolderWelcomeUiState(
                    qrData = BASE64_ENCODED_DEVICE_ENGAGEMENT
                ),
                modifier = Modifier,
                multiplePermissionsState = fakeState,
                hasPreviouslyRequestedPermission = true
            ) {}
        }
        composeTestRule.assertWelcomeTextIsDisplayed()
        composeTestRule.assertQrCodeIsDisplayed()
    }

    @Test
    fun previewUsage() = runTest {
        composeTestRule.setContent {
            HolderWelcomeScreenPreview()
        }

        composeTestRule.onNodeWithContentDescription("QR Data")
    }
}
