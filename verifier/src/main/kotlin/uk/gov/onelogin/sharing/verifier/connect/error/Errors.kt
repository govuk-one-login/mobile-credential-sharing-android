package uk.gov.onelogin.sharing.verifier.connect.error

import android.content.Context
import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceError

@StringRes
internal fun errorTitleRes(error: ConnectWithHolderDeviceError): Int = when (error) {
    ConnectWithHolderDeviceError.BluetoothConfigurationError ->
        R.string.bluetooth_connection_error_invalid_configuration

    ConnectWithHolderDeviceError.GenericError ->
        R.string.bluetooth_connection_error_generic

    ConnectWithHolderDeviceError.BluetoothConnectionError ->
        R.string.bluetooth_disconnected_unexpectedly
}

internal fun errorTitle(context: Context, error: ConnectWithHolderDeviceError): String =
    context.getString(errorTitleRes(error))
