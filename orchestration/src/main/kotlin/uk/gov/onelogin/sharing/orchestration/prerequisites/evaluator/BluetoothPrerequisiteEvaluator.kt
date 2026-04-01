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
    permissionChecker: PermissionChecker
) : PermissionChecker by permissionChecker,
    PrerequisiteEvaluator<BluetoothState> {
    override fun evaluate(): BluetoothState? = evaluatePermissions()
        ?: evaluateSupport()
        ?: evaluateRestrictions()
        ?: evaluateReadiness()

    private fun evaluatePermissions(): BluetoothState? =
        BluetoothPermissionChecker.Companion.bluetoothPermissions()
            .let(::checkPermissions).let { result ->
                when (result) {
                    PermissionChecker.Response.Passed -> null
                    is PermissionChecker.Response.Missing -> BluetoothState.PermissionNotGranted
                }
            }

    private fun evaluateSupport(): BluetoothState? =
        if (context.bluetoothManager?.adapter == null) {
            BluetoothState.Unsupported
        } else {
            null
        }

    private fun evaluateRestrictions(): BluetoothState? = if (
        context.userManager?.hasUserRestriction(UserManager.DISALLOW_BLUETOOTH) ?: true
    ) {
        BluetoothState.Restricted
    } else {
        null
    }

    private fun evaluateReadiness(): BluetoothState? = if (
        context.bluetoothManager?.adapter?.isEnabled ?: false
    ) {
        null
    } else {
        BluetoothState.PoweredOff
    }
}
