package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Convenience object for holding various [ConnectWithHolderDeviceState] objects for testing
 * purposes.
 */
@OptIn(ExperimentalPermissionsApi::class)
data object ConnectWithHolderDeviceStateStubs {
    val undecodableState = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        hasAllPermissions = false
    )

    val decodableDeniedState = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        hasAllPermissions = false
    )

    val decodableGrantedState = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        hasAllPermissions = true
    )

    val validWithCorrectBluetoothSetup = ConnectWithHolderDeviceState(
        isBluetoothEnabled = true,
        hasAllPermissions = true
    )

    val genericErrorState = ConnectWithHolderDeviceState(
        hasAllPermissions = true,
        isBluetoothEnabled = true
    )
}
