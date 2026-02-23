package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothCentralPermissionChecker.Companion.centralPermissions
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPeripheralPermissionChecker.Companion.peripheralPermissions
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker

/**
 * [uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker] implementation for use in Android-powered devices running
 * [android.os.Build.VERSION_CODES.S] or higher.
 */
@RequiresApi(Build.VERSION_CODES.S)
internal class Api31BluetoothPermissionChecker(
    private val context: Context,
) : BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult =
        checkPermissions(peripheralPermissions())

    override fun checkCentralPermissions(): PermissionCheckerResult =
        checkPermissions(centralPermissions())

    private fun checkPermissions(
        permissionsToCheck: List<String>
    ): PermissionCheckerResult {
        val missingPermissions = permissionsToCheck
            .map { permission ->
                permission to ContextCompat.checkSelfPermission(context, permission)
            }.filterNot { (_, permissionState) ->
                PackageManager.PERMISSION_GRANTED == permissionState
            }.map { (permission, _) ->
                permission
            }

        return if (missingPermissions.isEmpty()) {
            PermissionCheckerResult.Passed
        } else {
            PermissionCheckerResult.Missing(missingPermissions)
        }
    }
}
