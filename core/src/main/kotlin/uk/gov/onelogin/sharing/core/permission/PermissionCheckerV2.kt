package uk.gov.onelogin.sharing.core.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


fun interface PermissionCheckerV2 {
    fun checkPermissions(permissions: List<String>): List<Response>
    fun checkPermissions(vararg permissions: String): List<Response> = checkPermissions(
        permissions.asList()
    )

    sealed interface Response {
        data object Granted : Response
        data class Denied(
            val permission: String,
            val shouldShowRationale: Boolean
        ) : Response
    }
}

class ActivityPermissionChecker(
    private val activity: Activity
) : PermissionCheckerV2 {
    override fun checkPermissions(
        permissions: List<String>
    ): List<PermissionCheckerV2.Response> = permissions.map { permission ->
        when {
            PackageManager.PERMISSION_GRANTED ==
                ActivityCompat.checkSelfPermission(activity, permission) ->
                    PermissionCheckerV2.Response.Granted
            else -> PermissionCheckerV2.Response.Denied(
                permission = permission,
                shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    permission
                )
            )
        }
    }
}

fun List<PermissionCheckerV2.Response>.hasGrantedPermissions(): Boolean = all {
    it == PermissionCheckerV2.Response.Granted
}

fun List<PermissionCheckerV2.Response>.hasDeniedPermissions(): Boolean = any {
    it is PermissionCheckerV2.Response.Denied && it.shouldShowRationale
}

fun List<PermissionCheckerV2.Response>.hasPermanentlyDeniedPermissions(): Boolean = any {
    it is PermissionCheckerV2.Response.Denied && !it.shouldShowRationale
}
