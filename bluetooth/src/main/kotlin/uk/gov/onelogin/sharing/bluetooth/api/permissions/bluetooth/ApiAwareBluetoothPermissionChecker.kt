package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.content.Context
import android.os.Build
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult

/**
 * [BluetoothPermissionChecker] implementation that defers to other implementations based on the
 * Android-powered device's [android.os.Build.VERSION.SDK_INT].
 */
class ApiAwareBluetoothPermissionChecker(private val context: Context) : BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult =
        calculateImplementation().checkPeripheralPermissions()

    override fun checkCentralPermissions(): PermissionCheckerResult =
        calculateImplementation().checkCentralPermissions()

    private fun calculateImplementation(): BluetoothPermissionChecker = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            Api31BluetoothPermissionChecker(context)
        else -> TruthyBluetoothPermissionChecker
    }
}