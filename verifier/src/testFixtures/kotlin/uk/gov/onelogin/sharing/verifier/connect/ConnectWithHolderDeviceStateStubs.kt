package uk.gov.onelogin.sharing.verifier.connect

import android.Manifest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import uk.gov.onelogin.sharing.core.presentation.permissions.FakeMultiplePermissionsState
import uk.gov.onelogin.sharing.core.presentation.permissions.FakePermissionState
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.undecodeableBarcodeDataResult
import uk.gov.onelogin.sharing.verifier.scan.state.data.BarcodeDataResultStubs.validBarcodeDataResult

/**
 * Convenience object for holding various [ConnectWithHolderDeviceState] objects for testing
 * purposes.
 */
@OptIn(ExperimentalPermissionsApi::class)
data object ConnectWithHolderDeviceStateStubs {
    /**
     * State for when the [ConnectWithHolderDeviceState.base64EncodedEngagement] cannot be
     * decoded for bluetooth connection purposes.
     */
    val undecodableState = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        base64EncodedEngagement = undecodeableBarcodeDataResult.data,
        hasAllPermissions = false
    )

    /**
     * State that includes a valid [ConnectWithHolderDeviceState.base64EncodedEngagement] for
     * bluetooth connection purposes.
     *
     * Doesn't grant bluetooth permissions.
     */
    val decodableDeniedState = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        base64EncodedEngagement = validBarcodeDataResult.data,
        hasAllPermissions = false
    )

    /**
     * State that includes a valid [ConnectWithHolderDeviceState.base64EncodedEngagement] for
     * bluetooth connection purposes.
     *
     * Also grants [android.Manifest.permission.BLUETOOTH_CONNECT] permissions.
     */
    val decodableGrantedState = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        base64EncodedEngagement = validBarcodeDataResult.data,
        hasAllPermissions = true
    )

    /**
     * State that includes a valid [ConnectWithHolderDeviceState.base64EncodedEngagement] for
     * bluetooth connection purposes.
     *
     * Also grants [android.Manifest.permission.BLUETOOTH_CONNECT] permissions.
     */
    val validWithCorrectBluetoothSetup = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        base64EncodedEngagement = validBarcodeDataResult.data,
        hasAllPermissions = true
    )

    val fakePermissionStateGranted = FakeMultiplePermissionsState(
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

    val fakePermissionStateDenied = FakeMultiplePermissionsState(
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

    val fakePermissionStateDeniedWithRationale = FakeMultiplePermissionsState(
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
