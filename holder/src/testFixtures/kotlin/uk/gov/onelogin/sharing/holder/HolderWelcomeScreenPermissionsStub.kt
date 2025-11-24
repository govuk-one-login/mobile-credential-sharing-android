package uk.gov.onelogin.sharing.holder

import android.Manifest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState

object HolderWelcomeScreenPermissionsStub {

    @OptIn(ExperimentalPermissionsApi::class)
    val fakeGrantedPermissionsState = FakeMultiplePermissionsState(
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
}