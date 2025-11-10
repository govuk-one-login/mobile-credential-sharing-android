package uk.gov.onelogin.sharing.bluetooth.permissions

import android.Manifest
import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

@TargetApi(Build.VERSION_CODES.S)
class BluetoothPermissionChecker(private val context: Context) : PermissionChecker {
    override fun hasPermission(): Boolean = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_ADVERTISE
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}
