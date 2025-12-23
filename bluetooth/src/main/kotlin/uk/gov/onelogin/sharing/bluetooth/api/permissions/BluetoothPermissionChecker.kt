package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

@SuppressLint("NewApi")
class BluetoothPermissionChecker(private val context: Context) : PermissionChecker {
    override fun hasPeripheralPermissions(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ).map { permission ->
                ContextCompat.checkSelfPermission(context, permission)
            }.all { PackageManager.PERMISSION_GRANTED == it }
        } else {
            true
        }

    override fun hasCentralPermissions(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_CONNECT
            ).map { permission ->
                ContextCompat.checkSelfPermission(context, permission)
            }.all { PackageManager.PERMISSION_GRANTED == it }
        } else {
            true
        }
}
