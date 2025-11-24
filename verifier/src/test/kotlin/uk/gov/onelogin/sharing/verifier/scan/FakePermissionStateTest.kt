package uk.gov.onelogin.sharing.verifier.scan

import android.Manifest
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState

@OptIn(ExperimentalPermissionsApi::class)
@RunWith(AndroidJUnit4::class)
class FakePermissionStateTest {

    private var hasLaunched = false

    private val state = FakePermissionState(
        status = PermissionStatus.Granted,
        permission = Manifest.permission.CAMERA,
        onLaunchPermission = { hasLaunched = true }
    )

    @Test
    fun launchPermissionRequestDefersToLambda() {
        state.launchPermissionRequest()

        assertTrue(hasLaunched)
    }
}
