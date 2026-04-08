package uk.gov.onelogin.sharing.core.presentation.bluetooth

import android.content.Context
import androidx.annotation.StringRes
import uk.gov.onelogin.sharing.core.R as coreR

/**
 * Returns the string resource ID for the error text for each [BluetoothSessionError].
 * This used in the [BluetoothConnectionErrorScreen]
 *
 * @param error The error to get the title for.
 * @return The string resource ID for the error title.
 */
@StringRes
fun errorTitleRes(error: BluetoothSessionError): Int = when (error) {
    BluetoothSessionError.BluetoothConfigurationError ->
        coreR.string.bluetooth_connection_error_failed

    BluetoothSessionError.GenericError ->
        coreR.string.bluetooth_connection_error_generic

    BluetoothSessionError.BluetoothConnectionError ->
        coreR.string.bluetooth_disconnected_unexpectedly

    BluetoothSessionError.BluetoothDisabledError ->
        coreR.string.bluetooth_turned_off_verifier

    BluetoothSessionError.BluetoothPermissionsError ->
        coreR.string.bluetooth_permissions_revoked
}

/**
 * Returns the title string for the given [BluetoothSessionError].
 *
 * @param context The context to use for retrieving the string.
 * @param error The error to get the title for.
 * @return The title string for the error.
 */
fun errorTitle(context: Context, error: BluetoothSessionError): String =
    context.getString(errorTitleRes(error))
