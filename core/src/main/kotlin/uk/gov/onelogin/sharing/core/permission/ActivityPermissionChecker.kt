package uk.gov.onelogin.sharing.core.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2.PermissionCheckResult

class ActivityPermissionChecker internal constructor(
    private val activity: Activity,
    private val markerStore: PermissionDenialMarkerStore
) : PermissionCheckerV2 {

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
                }.also {
                    markerStore.mark(permission)
                }
            }
        }
}
