package uk.gov.onelogin.sharing.prerequisites.api.state

import android.Manifest
import uk.gov.onelogin.sharing.prerequisites.api.Actionable
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.Recoverable

/**
 * Prerequisite evaluation result that's specific to Location capabilities.
 */
enum class LocationState :
    Recoverable,
    Actionable<PrerequisiteAction> {
    /**
     * The Android-powered device doesn't support location capabilities.
     */
    Unsupported,
    ServicesDisabled,
    PermissionDeniedPermanently,
    PermissionNotGranted,
    PermissionUndetermined;

    override fun isRecoverable(): Boolean = this in recoverabilityMap.keys

    override fun getAction(): PrerequisiteAction? = recoverabilityMap[this]

    companion object {
        private val requestPermissionsAction = PrerequisiteAction.RequestPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        @JvmStatic
        private val recoverabilityMap = mapOf(
            PermissionDeniedPermanently to PrerequisiteAction.OpenAppPermissions,
            PermissionNotGranted to requestPermissionsAction,
            PermissionUndetermined to requestPermissionsAction,
            ServicesDisabled to PrerequisiteAction.EnableLocationServices
        )
    }
}
