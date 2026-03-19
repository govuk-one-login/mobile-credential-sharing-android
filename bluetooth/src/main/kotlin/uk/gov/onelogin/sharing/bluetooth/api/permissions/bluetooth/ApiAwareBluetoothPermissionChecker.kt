package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.Api31BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth.truthyBluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.HolderUiScope
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.permission.PermissionChecker

/**
 * [BluetoothPermissionChecker] implementation that defers to other implementations based on the
 * Android-powered device's [android.os.Build.VERSION.SDK_INT].
 */
@ContributesBinding(AppScope::class, binding = binding<BluetoothPermissionChecker>())
@ContributesBinding(HolderUiScope::class, binding = binding<BluetoothPermissionChecker>())
@ContributesBinding(VerifierUiScope::class, binding = binding<BluetoothPermissionChecker>())
class ApiAwareBluetoothPermissionChecker(private val checker: PermissionChecker) :
    BluetoothPermissionChecker {

    override fun checkBluetoothPermissions(): PermissionChecker.Response =
        calculateImplementation().checkBluetoothPermissions()

    internal fun calculateImplementation(): BluetoothPermissionChecker = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> truthyBluetoothPermissionChecker
        else -> Api31BluetoothPermissionChecker(checker)
    }
}
