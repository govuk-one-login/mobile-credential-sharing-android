package uk.gov.onelogin.sharing.verifier.connect.error

import android.content.Context
import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.core.R as coreR
import uk.gov.onelogin.sharing.verifier.R
import uk.gov.onelogin.sharing.verifier.connect.ConnectWithHolderDeviceError

/**
 * Returns the string resource ID for the error text for each [ConnectWithHolderDeviceError].
 * This used in the [BluetoothConnectionErrorScreen]
 *
 * @param error The error to get the title for.
 * @return The string resource ID for the error title.
 */
@StringRes
internal fun errorTitleRes(error: ConnectWithHolderDeviceError): Int = when (error) {
    ConnectWithHolderDeviceError.BluetoothConfigurationError ->
        R.string.bluetooth_connection_error_failed

    ConnectWithHolderDeviceError.GenericError ->
        R.string.bluetooth_connection_error_generic

    ConnectWithHolderDeviceError.BluetoothConnectionError ->
        coreR.string.bluetooth_disconnected_unexpectedly

    ConnectWithHolderDeviceError.BluetoothDisabledError ->
        R.string.bluetooth_turned_off_verifier

    ConnectWithHolderDeviceError.BluetoothPermissionsError ->
        coreR.string.bluetooth_permissions_revoked
}

/**
 * Returns the title string for the given [ConnectWithHolderDeviceError].
 *
 * @param context The context to use for retrieving the string.
 * @param error The error to get the title for.
 * @return The title string for the error.
 */
internal fun errorTitle(context: Context, error: ConnectWithHolderDeviceError): String =
    context.getString(errorTitleRes(error))
