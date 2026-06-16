package uk.gov.onelogin.sharing.prerequisites.evaluator

import android.content.Context
import android.os.UserManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import uk.gov.onelogin.sharing.bluetooth.ContextExt.bluetoothManager
import uk.gov.onelogin.sharing.bluetooth.ContextExt.userManager
import uk.gov.onelogin.sharing.prerequisites.permissions.BluetoothPermissions.getBluetoothPermissions
import uk.gov.onelogin.sharing.prerequisites.permissions.IterablePermissionsExt.hasPermanentlyDeniedPermissions
import uk.gov.onelogin.sharing.prerequisites.permissions.IterablePermissionsExt.hasUndeterminedPermissions
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionChecker
import uk.gov.onelogin.sharing.prerequisites.state.BluetoothState

@ContributesBinding(AppScope::class, binding = binding<PrerequisiteEvaluator<BluetoothState>>())
@Inject
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
        getBluetoothPermissions().let(::checkPermissions).let { result ->
            when {
                result.isEmpty() -> null

                result.hasPermanentlyDeniedPermissions() ->
                    BluetoothState.PermissionDeniedPermanently

                result.hasUndeterminedPermissions() -> BluetoothState.PermissionUndetermined

                else -> BluetoothState.PermissionNotGranted
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
