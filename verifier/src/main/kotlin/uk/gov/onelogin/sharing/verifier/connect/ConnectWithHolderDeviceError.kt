package uk.gov.onelogin.sharing.verifier.connect

/**
 * Sealed class for the different kinds of errors that appear within the
 * [ConnectWithHolderDeviceScreen] composable UI.
 */
sealed interface ConnectWithHolderDeviceError {
    /**
     * Declares that a mismatch occurred between the expected
     * [android.bluetooth.BluetoothGattCharacteristic]s and those provided by the holder device.
     */
    data object BluetoothConfigurationError : ConnectWithHolderDeviceError

    /**
     * Declares that an unknown error occurred when scanning for a bluetooth device.
     */
    data object GenericError : ConnectWithHolderDeviceError

    /**
     * Declares that a bluetooth connection error has occurred.
     */
    data object BluetoothConnectionError : ConnectWithHolderDeviceError

    /**
     * Declares that a bluetooth has been disabled.
     */
    data object BluetoothDisabledError : ConnectWithHolderDeviceError

    /**
     * Declares that a bluetooth permissions have been denied.
     */
    data object BluetoothPermissionsError : ConnectWithHolderDeviceError
}
