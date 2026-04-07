package uk.gov.onelogin.sharing.core.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


fun interface PermissionCheckerV2 {
    /**
     * @return An empty [List] when all requested [permissions] are granted. Otherwise, a list of
     * [Denied] objects
     */
    fun checkPermissions(permissions: List<String>): List<Denied>

    /**
     * @return An empty [List] when all requested [permissions] are granted. Otherwise, a list of
     * [Denied] objects
     */
    fun checkPermissions(vararg permissions: String): List<Denied> = checkPermissions(
        permissions.asList()
    )

    data class Denied(
        val permission: String,
        val shouldShowRationale: Boolean,
    )
}

class ActivityPermissionChecker(
    private val activity: Activity,
) : PermissionCheckerV2 {
    override fun checkPermissions(
        permissions: List<String>,
    ): List<PermissionCheckerV2.Denied> = permissions
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

class FakePermissionChecker(
    private val missingPermissions: () -> List<PermissionCheckerV2.Denied> = { emptyList() },
) : PermissionCheckerV2 {
    override fun checkPermissions(
        permissions: List<String>,
    ): List<PermissionCheckerV2.Denied> = missingPermissions().filter {
        it.permission in permissions
    }
}

fun Iterable<String>.toDeniedPermission(
    shouldShowRationale: Boolean = true
) : List<PermissionCheckerV2.Denied> = map {
    PermissionCheckerV2.Denied(
        permission = it,
        shouldShowRationale = shouldShowRationale
    )
}

fun Iterable<PermissionCheckerV2.Denied>.toPermissionsList(): List<String> =
    map(PermissionCheckerV2.Denied::permission)

fun Iterable<PermissionCheckerV2.Denied>.hasDeniedPermissions(): Boolean = any {
    it.shouldShowRationale
}

fun Iterable<PermissionCheckerV2.Denied>.hasPermanentlyDeniedPermissions(): Boolean = any {
    !it.shouldShowRationale
}
