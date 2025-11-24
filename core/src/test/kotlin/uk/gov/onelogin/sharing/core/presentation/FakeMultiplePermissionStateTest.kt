package uk.gov.onelogin.sharing.core.presentation

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class FakeMultiplePermissionStateTest {

    private var hasLaunched = false

    val state = FakeMultiplePermissionsState(
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
        onLaunchPermission = { hasLaunched = true }
    )

    @Test
    fun `allPermissionsGranted should be true when all permissions granted`() {
        assertTrue { state.allPermissionsGranted }
    }

    @Test
    fun `shouldShowRationale should be true if any permission requires rationale`() {
        val state = FakeMultiplePermissionsState(
            permissions = listOf(
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_CONNECT,
                    status = PermissionStatus.Granted
                ),
                FakePermissionState(
                    permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                    status = PermissionStatus.Denied(true)
                )
            ),
            onLaunchPermission = { hasLaunched = true }
        )

        assertTrue { state.shouldShowRationale }
    }

    @Test
    fun `revokedPermissions should return a list of non-granted permissions`() {
        val grantedPermission =
            FakePermissionState(Manifest.permission.BLUETOOTH_ADVERTISE, PermissionStatus.Granted)
        val deniedPermission = FakePermissionState(
            Manifest.permission.BLUETOOTH_CONNECT,
            PermissionStatus.Denied(false)
        )

        val state = FakeMultiplePermissionsState(
            permissions = listOf(grantedPermission, deniedPermission),
            onLaunchPermission = { }
        )

        val revoked = state.revokedPermissions

        assertEquals(1, revoked.size)
    }

    @Test
    fun launchPermissionRequestDefersToLambda() {
        state.launchMultiplePermissionRequest()

        Assert.assertTrue(hasLaunched)
    }
}
