package uk.gov.onelogin.sharing.prerequisites.api.state

import uk.gov.onelogin.sharing.prerequisites.Actionable
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.Recoverable
import uk.gov.onelogin.sharing.prerequisites.api.permissions.BluetoothPermissions.getBluetoothPermissions

/**
 * Prerequisite evaluation result that's specific to Bluetooth capabilities.
 */
enum class BluetoothState :
    Recoverable,
    Actionable<PrerequisiteAction> {

    /**
     * The Android-powered device doesn't support bluetooth capabilities.
     */
    Unsupported,

    /**
     * Organizational / account restrictions stop bluetooth capabilities from working.
     */
    Restricted,

    /**
     * Bluetooth capabilities are currently stopped on the Android-powered device.
     */
    PoweredOff,

    /**
     * Permissions have reached a terminally denied state, requiring User intervention within the
     * app's settings.
     */
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
