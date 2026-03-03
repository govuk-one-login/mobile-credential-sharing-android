package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import android.os.Build
import androidx.annotation.RequiresApi
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker.Companion.bluetoothPermissions
import uk.gov.onelogin.sharing.core.permission.PermissionChecker

/**
 * [BluetoothPermissionChecker] implementation for use in Android-powered devices running
 * [android.os.Build.VERSION_CODES.S] or higher.
 */
@RequiresApi(Build.VERSION_CODES.S)
internal class Api31BluetoothPermissionChecker(checker: PermissionChecker) :
    BluetoothPermissionChecker,
    PermissionChecker by checker {

    override fun checkBluetoothPermissions(): PermissionChecker.Response =
        checkPermissions(bluetoothPermissions())
}
