package uk.gov.onelogin.sharing.prerequisites.state

import android.Manifest
import uk.gov.onelogin.sharing.prerequisites.Actionable
import uk.gov.onelogin.sharing.prerequisites.PrerequisiteAction
import uk.gov.onelogin.sharing.prerequisites.Recoverable

enum class CameraState :
    Recoverable,
    Actionable<PrerequisiteAction> {
    Unsupported,
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
