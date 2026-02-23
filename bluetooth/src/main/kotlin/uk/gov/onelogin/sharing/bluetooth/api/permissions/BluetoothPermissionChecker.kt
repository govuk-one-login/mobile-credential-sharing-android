package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.content.Context
import android.os.Build

/**
 * [PermissionChecker] implementation that defers to other implementations based on the
 * Android-powered device's [Build.VERSION.SDK_INT].
 */
class BluetoothPermissionChecker(private val context: Context) : PermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult =
        calculateImplementation().checkPeripheralPermissions()

    override fun hasCentralPermissions(): Boolean =
        calculateImplementation().hasCentralPermissions()

    private fun calculateImplementation(): PermissionChecker = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            Api31BluetoothPermissionChecker(context)
        else -> TruthyBluetoothPermissionChecker
    }
}
