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
    )
}
