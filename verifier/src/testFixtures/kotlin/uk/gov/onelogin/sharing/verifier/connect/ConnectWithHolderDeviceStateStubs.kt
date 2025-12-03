package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
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
        base64EncodedEngagement = undecodeableBarcodeDataResult.data,
        permissionState = FakePermissionState.Companion.bluetoothConnect(
            status = PermissionStatus.Denied(shouldShowRationale = false)
        )
    )

    /**
     * State that includes a valid [ConnectWithHolderDeviceState.base64EncodedEngagement] for
     * bluetooth connection purposes.
     *
     * Doesn't grant bluetooth permissions.
     */
    val decodableDeniedState = ConnectWithHolderDeviceState(
        base64EncodedEngagement = validBarcodeDataResult.data,
        permissionState = FakePermissionState.Companion.bluetoothConnect(
            status = PermissionStatus.Denied(shouldShowRationale = false)
        )
    )

    /**
     * State that includes a valid [ConnectWithHolderDeviceState.base64EncodedEngagement] for
     * bluetooth connection purposes.
     *
     * Also grants [android.Manifest.permission.BLUETOOTH_CONNECT] permissions.
     */
    val decodableGrantedState = ConnectWithHolderDeviceState(
        base64EncodedEngagement = validBarcodeDataResult.data,
        permissionState = FakePermissionState.Companion.bluetoothConnect(
            status = PermissionStatus.Granted
        )
    )
}
