package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

/**
 * [BluetoothPermissionChecker] implementation for use in Android-powered devices lower than
 * [android.os.Build.VERSION_CODES.S].
 */
internal val truthyBluetoothPermissionChecker: BluetoothPermissionChecker =
    BluetoothPermissionChecker {
        Response.Passed
    }
