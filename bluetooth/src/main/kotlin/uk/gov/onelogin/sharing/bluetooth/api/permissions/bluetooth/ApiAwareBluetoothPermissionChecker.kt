package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.Api31BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.TruthyBluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker

/**
 * [BluetoothPermissionChecker] implementation that defers to other implementations based on the
 * Android-powered device's [android.os.Build.VERSION.SDK_INT].
 */
@ContributesBinding(AppScope::class, binding = binding<BluetoothCentralPermissionChecker>())
@ContributesBinding(AppScope::class, binding = binding<BluetoothPeripheralPermissionChecker>())
@ContributesBinding(AppScope::class, binding = binding<BluetoothPermissionChecker>())
@ContributesBinding(ViewModelScope::class, binding = binding<BluetoothCentralPermissionChecker>())
@ContributesBinding(
    ViewModelScope::class,
    binding = binding<BluetoothPeripheralPermissionChecker>()
)
@ContributesBinding(ViewModelScope::class, binding = binding<BluetoothPermissionChecker>())
class ApiAwareBluetoothPermissionChecker(private val checker: PermissionChecker) :
    BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): PermissionChecker.Response =
        calculateImplementation().checkPeripheralPermissions()

    override fun checkCentralPermissions(): PermissionChecker.Response =
        calculateImplementation().checkCentralPermissions()

    internal fun calculateImplementation(): BluetoothPermissionChecker = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ->
            TruthyBluetoothPermissionChecker

        else -> Api31BluetoothPermissionChecker(checker)
    }
}
