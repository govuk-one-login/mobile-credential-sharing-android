package uk.gov.onelogin.sharing.core.permission

import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2.PermissionCheckResult

object IterablePermissionsExt {
    fun Iterable<PermissionCheckResult>.toPermissionsList(): List<String> =
        map(PermissionCheckResult::permission)

    fun Iterable<PermissionCheckResult>.hasDeniedPermissions(): Boolean = any {
        it is PermissionCheckResult.Denied
    }

    fun Iterable<PermissionCheckResult>.hasPermanentlyDeniedPermissions(): Boolean = any {
        it is PermissionCheckResult.PermanentlyDenied
    }

    fun Iterable<PermissionCheckResult>.hasUndeterminedPermissions(): Boolean = any {
        it is PermissionCheckResult.Undetermined
    }
}
