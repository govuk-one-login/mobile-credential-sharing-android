package uk.gov.onelogin.sharing.core.permission

object IterablePermissionsExt {
    fun Iterable<PermissionCheckerV2.Denied>.toPermissionsList(): List<String> =
        map(PermissionCheckerV2.Denied::permission)

    fun Iterable<PermissionCheckerV2.Denied>.hasDeniedPermissions(): Boolean = any {
        it.shouldShowRationale
    }

    fun Iterable<PermissionCheckerV2.Denied>.hasPermanentlyDeniedPermissions(): Boolean = any {
        !it.shouldShowRationale
    }
}
