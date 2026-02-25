package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import android.os.Build
import androidx.annotation.RequiresApi
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothCentralPermissionChecker.Companion.centralPermissions
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPeripheralPermissionChecker.Companion.peripheralPermissions
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker

/**
 * [BluetoothPermissionChecker] implementation for use in Android-powered devices running
 * [android.os.Build.VERSION_CODES.S] or higher.
 */
@RequiresApi(Build.VERSION_CODES.S)
internal class Api31BluetoothPermissionChecker(checker: PermissionChecker) :
    BluetoothPermissionChecker,
    PermissionChecker by checker {
    override fun checkPeripheralPermissions(): PermissionChecker.Response =
        checkPermissions(peripheralPermissions())

    override fun checkCentralPermissions(): PermissionChecker.Response =
        checkPermissions(centralPermissions())
}
