package uk.gov.onelogin.sharing.verifier.connect

import com.google.accompanist.permissions.ExperimentalPermissionsApi

/**
 * Property bag data class for holding the [ConnectWithHolderDeviceScreen] composable UI state.
 *
 * @param base64EncodedEngagement The CBOR string that's embedded within a valid digital credential
 * QR code.
 * @param showErrorScreen Used for navigating away from [ConnectWithHolderDeviceScreen] when the
 * parameter isn't null. Defaults to null, meaning that the User should be shown
 * [ConnectWithHolderDeviceScreen].
 */
@OptIn(ExperimentalPermissionsApi::class)
data class ConnectWithHolderDeviceState(
    val isBluetoothEnabled: Boolean = false,
    val base64EncodedEngagement: String? = null,
    val hasAllPermissions: Boolean = false,
    val hasRequestedPermissions: Boolean = false,
    val showErrorScreen: ConnectWithHolderDeviceError? = null
)

/**
 * Sealed class for the different kinds of errors that appear within the
 * [ConnectWithHolderDeviceScreen] composable UI.
 */
sealed class ConnectWithHolderDeviceError {
    /**
     * Declares that a mismatch occurred between the expected
     * [android.bluetooth.BluetoothGattCharacteristic]s and those provided by the holder device.
     */
    data object BluetoothConfigurationError : ConnectWithHolderDeviceError()

    /**
     * Declares that an unknown error occurred when scanning for a bluetooth device.
     */
    data object GenericError : ConnectWithHolderDeviceError()
}
