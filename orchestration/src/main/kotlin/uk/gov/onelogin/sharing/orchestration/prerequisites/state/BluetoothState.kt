package uk.gov.onelogin.sharing.orchestration.prerequisites.state

import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.Actionable
import uk.gov.onelogin.sharing.core.Recoverable
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction

enum class BluetoothState :
    Recoverable,
    Actionable<PrerequisiteAction> {
    Unsupported,
    Restricted,
    PoweredOff,
    PermissionNotGranted,
    PermissionDeniedPermanently;

    override fun isRecoverable(): Boolean = this in recoverabilityMap.keys

    override fun getAction(): PrerequisiteAction? = recoverabilityMap[this]

    companion object {
        @JvmStatic
        private val recoverabilityMap: Map<BluetoothState, PrerequisiteAction> = mapOf(
            PermissionNotGranted to PrerequisiteAction.RequestPermissions(
                BluetoothPermissionChecker.Companion.bluetoothPermissions()
            ),
            PoweredOff to PrerequisiteAction.EnableBluetooth,
            PermissionDeniedPermanently to PrerequisiteAction.OpenAppPermissions
        )
    }
}
