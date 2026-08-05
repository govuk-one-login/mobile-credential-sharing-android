package uk.gov.onelogin.sharing.prerequisites.impl.permissions

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker.PermissionCheckResult
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionDenialMarkerStore

class ActivityPermissionChecker internal constructor(
    private val activity: Activity,
    private val markerStore: PermissionDenialMarkerStore
) : PermissionChecker {

    constructor(
        activity: Activity
    ) : this(
        activity = activity,
        markerStore = SharedPreferencesPermissionStore(activity.applicationContext)
    )

    override fun checkPermissions(permissions: List<String>): List<PermissionCheckResult> =
        permissions.mapNotNull { permission ->
            val granted = ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                markerStore.clear(permission)
                null
            } else {
                val shouldShowRationale = ActivityCompat
                    .shouldShowRequestPermissionRationale(activity, permission)
                when {
                    permission !in markerStore && !shouldShowRationale ->
                        PermissionCheckResult::Undetermined

                    shouldShowRationale -> PermissionCheckResult::Denied

                    else -> PermissionCheckResult::PermanentlyDenied
                }.let { constructor ->
                    constructor(permission)
                }
            }
        }

    override fun markAsRequested(permissions: List<String>) {
        permissions.forEach(markerStore::mark)
    }
}
