package uk.gov.onelogin.sharing.orchestration.prerequisites.evaluator

import android.content.Context
import android.os.UserManager
import uk.gov.onelogin.sharing.bluetooth.ContextExt.bluetoothManager
import uk.gov.onelogin.sharing.bluetooth.ContextExt.userManager
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker
import uk.gov.onelogin.sharing.orchestration.prerequisites.state.BluetoothState

class BluetoothPrerequisiteEvaluator(
    private val context: Context,
    permissionChecker: PermissionChecker,
) : PermissionChecker by permissionChecker,
    PrerequisiteEvaluator<BluetoothState> {
    override fun evaluate(): BluetoothState? =
        evaluateBluetoothPermissions()
            ?: evaluateBluetoothSupport()
            ?: evaluateBluetoothRestrictions()
            ?: evaluateBluetoothReadiness()

    private fun evaluateBluetoothPermissions(): BluetoothState? =
        BluetoothPermissionChecker.Companion.bluetoothPermissions()
            .let(::checkPermissions).let { result ->
            when (result) {
                PermissionChecker.Response.Passed -> null
                is PermissionChecker.Response.Missing -> BluetoothState.PermissionNotGranted
            }
        }

    private fun evaluateBluetoothSupport(): BluetoothState? =
        context.bluetoothManager?.adapter?.let {
            BluetoothState.Unsupported
        }

    private fun evaluateBluetoothRestrictions(): BluetoothState? = if (
        context.userManager?.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH) ?: true
    ) {
        BluetoothState.Restricted
    } else {
        null
    }

    private fun evaluateBluetoothReadiness(): BluetoothState? = if (
            context.bluetoothManager?.adapter?.isEnabled ?: false
        ) {
            null
        } else {
        BluetoothState.PoweredOff
    }
}
