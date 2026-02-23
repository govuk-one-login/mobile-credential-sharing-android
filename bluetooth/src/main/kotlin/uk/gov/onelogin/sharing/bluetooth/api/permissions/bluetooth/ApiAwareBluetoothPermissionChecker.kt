package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.content.Context
import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.Api31BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.TruthyBluetoothPermissionChecker

/**
 * [BluetoothPermissionChecker] implementation that defers to other implementations based on the
 * Android-powered device's [android.os.Build.VERSION.SDK_INT].
 */
@ContributesBinding(AppScope::class)
@ContributesBinding(ViewModelScope::class)
class ApiAwareBluetoothPermissionChecker(private val context: Context) : BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult =
        calculateImplementation().checkPeripheralPermissions()

    override fun checkCentralPermissions(): PermissionCheckerResult =
        calculateImplementation().checkCentralPermissions()

    internal fun calculateImplementation(): BluetoothPermissionChecker = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ->
            TruthyBluetoothPermissionChecker
        else -> Api31BluetoothPermissionChecker(context)
    }
}