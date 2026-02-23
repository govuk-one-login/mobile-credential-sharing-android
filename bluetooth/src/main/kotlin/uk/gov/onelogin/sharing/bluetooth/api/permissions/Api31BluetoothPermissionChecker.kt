package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothCentralPermissionChecker.Companion.centralPermissions
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPeripheralPermissionChecker.Companion.peripheralPermissions

/**
 * [PermissionChecker] implementation for use in Android-powered devices running
 * [android.os.Build.VERSION_CODES.S] or higher.
 */
@RequiresApi(Build.VERSION_CODES.S)
internal class Api31BluetoothPermissionChecker(
    private val context: Context,
) : PermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult {
        val missingPermissions = peripheralPermissions()
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

    override fun hasCentralPermissions(): Boolean = centralPermissions()
        .map { permission ->
            ContextCompat.checkSelfPermission(context, permission)
        }.all { PackageManager.PERMISSION_GRANTED == it }
}