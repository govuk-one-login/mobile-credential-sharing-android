package uk.gov.onelogin.sharing.core.presentation.bluetooth

/**
 * Sealed class for the different kinds of errors that appear within the UI.
 */
sealed interface BluetoothSessionError {
    /**
     * Declares that a mismatch occurred between the expected
     * [android.bluetooth.BluetoothGattCharacteristic]s and those provided by the holder device.
     */
    data object BluetoothConfigurationError : BluetoothSessionError

    /**
     * Declares that an unknown error occurred when scanning for a bluetooth device.
     */
    data object GenericError : BluetoothSessionError

    /**
     * Declares that a bluetooth connection error has occurred.
     */
    data object BluetoothConnectionError : BluetoothSessionError

    /**
     * Declares that a bluetooth has been disabled.
     */
    data object BluetoothDisabledError : BluetoothSessionError

    /**
     * Declares that a bluetooth permissions have been denied.
     */
    data object BluetoothPermissionsError : BluetoothSessionError
}
