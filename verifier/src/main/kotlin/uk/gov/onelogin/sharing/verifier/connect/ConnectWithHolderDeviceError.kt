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
     * 'Null-object' state for [ConnectWithHolderDeviceError], meaning that there's no error.
     */
    data object NoError : ConnectWithHolderDeviceError
}
