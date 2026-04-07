package uk.gov.onelogin.sharing.core.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class ActivityPermissionChecker(private val activity: Activity) : PermissionCheckerV2 {
    override fun checkPermissions(permissions: List<String>): List<PermissionCheckerV2.Denied> =
        permissions
            .filterNot { permission ->
                PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
                    activity,
                    permission
                )
            }.map { permission ->
                PermissionCheckerV2.Denied(
                    permission = permission,
                    shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        permission
                    )
                )
            }
}
