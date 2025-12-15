package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 *
 * @param base64EncodedEngagement The CBOR string that's embedded within a valid digital credential
 * QR code.
 * @param permissionState The Android-powered device's bluetooth permission state.
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val adapter: BluetoothAdapterProvider? = null,
    val isBluetoothEnabled: Boolean = false,
    val base64EncodedEngagement: String? = null,
    val permissionState: PermissionState? = null
)
