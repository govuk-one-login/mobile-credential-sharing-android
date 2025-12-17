package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi
import uk.gov.onelogin.sharing.bluetooth.api.adapter.BluetoothAdapterProvider

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 *
 * @param base64EncodedEngagement The CBOR string that's embedded within a valid digital credential
 * QR code.
 * @param permissionSCtate The Android-powered device's bluetooth permission state.
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val isBluetoothEnabled: Boolean = false,
    val base64EncodedEngagement: String? = null,
    val hasAllPermissions: Boolean = false,
    val hasRequestedPermissions: Boolean = false,
    val showErrorScreen: Boolean = false
)
