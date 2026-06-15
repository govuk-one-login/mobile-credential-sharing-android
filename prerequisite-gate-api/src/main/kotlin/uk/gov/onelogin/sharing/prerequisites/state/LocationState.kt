package uk.gov.onelogin.sharing.prerequisites.state

import android.Manifest
import uk.gov.onelogin.sharing.prerequisites.Actionable
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.Recoverable

enum class LocationState :
    Recoverable,
    Actionable<PrerequisiteAction> {
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
