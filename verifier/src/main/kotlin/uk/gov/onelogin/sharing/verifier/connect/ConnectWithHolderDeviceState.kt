package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 *
 * @param base64EncodedEngagement The CBOR string that's embedded within a valid digital credential
 * QR code.
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val isBluetoothEnabled: Boolean = false,
    val base64EncodedEngagement: String? = null,
    val hasAllPermissions: Boolean = false,
    val hasRequestedPermissions: Boolean = false,
    val showErrorScreen: Boolean = false
)

sealed class DeviceState {
    data class Loaded(
        val isBluetoothEnabled: Boolean = false,
        val base64EncodedEngagement: String? = null,
        val hasAllPermissions: Boolean = false,
        val hasRequestedPermissions: Boolean = false
    ) : DeviceState()

    data object BluetoothError : DeviceState()
    data object GenericError : DeviceState()
    data object RequiresPermissions : DeviceState()
}
