package uk.gov.onelogin.sharing.prerequisites.api.state

import android.Manifest
import uk.gov.onelogin.sharing.prerequisites.api.Actionable
import uk.gov.onelogin.sharing.prerequisites.api.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.api.Recoverable

/**
 * Prerequisite evaluation result that's specific to Camera capabilities.
 */
enum class CameraState :
    Recoverable,
    Actionable<PrerequisiteAction> {
    /**
     * The Android-powered device doesn't support camera capabilities.
     */
    Unsupported,

    /**
     * Organizational / account restrictions stop camera capabilities from working.
     */
    Restricted,
    PermissionDeniedPermanently,
    PermissionNotGranted,
    PermissionUndetermined;

    override fun isRecoverable(): Boolean = this in recoverabilityMap.keys

    override fun getAction(): PrerequisiteAction? = recoverabilityMap[this]

    companion object {
        private val requestPermissionsAction = PrerequisiteAction.RequestPermissions(
            Manifest.permission.CAMERA
        )

        @JvmStatic
        private val recoverabilityMap = mapOf(
            PermissionDeniedPermanently to PrerequisiteAction.OpenAppPermissions,
            PermissionNotGranted to requestPermissionsAction,
            PermissionUndetermined to requestPermissionsAction
        )
    }
}
