package uk.gov.onelogin.sharing.orchestration.prerequisites.state

import android.Manifest
import uk.gov.onelogin.sharing.core.Actionable
import uk.gov.onelogin.sharing.core.Recoverable
import uk.gov.onelogin.sharing.orchestration.prerequisites.PrerequisiteAction

enum class LocationState :
    Recoverable,
    Actionable<PrerequisiteAction> {
    Unsupported,
    ServicesDisabled,
    PermissionNotGranted,
    PermissionDeniedPermanently;

    override fun isRecoverable(): Boolean = this in recoverabilityMap.keys

    override fun getAction(): PrerequisiteAction? = recoverabilityMap[this]

    companion object {
        @JvmStatic
        private val recoverabilityMap = mapOf(
            PermissionNotGranted to PrerequisiteAction.RequestPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PermissionDeniedPermanently to PrerequisiteAction.OpenAppPermissions,
            ServicesDisabled to PrerequisiteAction.EnableLocationServices
        )
    }
}
