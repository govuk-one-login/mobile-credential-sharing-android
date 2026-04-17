package uk.gov.onelogin.sharing.orchestration.prerequisites.state

import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissions.getBluetoothPermissions
import uk.gov.onelogin.sharing.core.Actionable
import uk.gov.onelogin.sharing.core.Recoverable
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction

enum class BluetoothState :
    Recoverable,
    Actionable<PrerequisiteAction> {
    Unsupported,
    Restricted,
    PoweredOff,
    PermissionDeniedPermanently,
    PermissionNotGranted,
    PermissionUndetermined;

    override fun isRecoverable(): Boolean = this in recoverabilityMap.keys

    override fun getAction(): PrerequisiteAction? = recoverabilityMap[this]

    companion object {
        private val requestPermissionsAction = PrerequisiteAction.RequestPermissions(
            getBluetoothPermissions()
        )

        @JvmStatic
        private val recoverabilityMap: Map<BluetoothState, PrerequisiteAction> = mapOf(
            PermissionDeniedPermanently to PrerequisiteAction.OpenAppPermissions,
            PermissionNotGranted to requestPermissionsAction,
            PermissionUndetermined to requestPermissionsAction,
            PoweredOff to PrerequisiteAction.EnableBluetooth
        )
    }
}
