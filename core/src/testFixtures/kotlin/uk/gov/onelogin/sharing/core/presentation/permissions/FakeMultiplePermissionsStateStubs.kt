package uk.gov.onelogin.sharing.core.presentation.permissions

import android.Manifest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus

@OptIn(ExperimentalPermissionsApi::class)
object FakeMultiplePermissionsStateStubs {
    val bluetoothPermissionsGranted = FakeMultiplePermissionsState(
        permissions = listOf(
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_CONNECT,
                status = PermissionStatus.Granted
            ),
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                status = PermissionStatus.Granted
            ),
            FakePermissionState(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                status = PermissionStatus.Granted
            )
        ),
        onLaunchPermission = {}
    )

    val bluetoothPermissionsDenied = FakeMultiplePermissionsState(
        permissions = listOf(
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_CONNECT,
                status = PermissionStatus.Denied(false)
            ),
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                status = PermissionStatus.Denied(false)
            ),
            FakePermissionState(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                status = PermissionStatus.Denied(false)
            )
        ),
        onLaunchPermission = {}
    )

    val bluetoothPermissionsDeniedWithRationale = FakeMultiplePermissionsState(
        permissions = listOf(
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_CONNECT,
                status = PermissionStatus.Denied(true)
            ),
            FakePermissionState(
                permission = Manifest.permission.BLUETOOTH_ADVERTISE,
                status = PermissionStatus.Denied(true)
            ),
            FakePermissionState(
                permission = Manifest.permission.ACCESS_FINE_LOCATION,
                status = PermissionStatus.Denied(true)
            )
        ),
        onLaunchPermission = {}
    )
}
