package uk.gov.onelogin.sharing.core.presentation

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
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
    fun launchPermissionRequestDefersToLambda() {
        state.launchMultiplePermissionRequest()

        Assert.assertTrue(hasLaunched)
    }
}
