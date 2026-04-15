package uk.gov.onelogin.sharing.core.permission

import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2.PermissionCheckResult.Denied
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2.PermissionCheckResult.PermanentlyDenied
import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2.PermissionCheckResult.Undetermined

object PermissionsToResultExt {
    fun Iterable<String>.toDeniedPermission(): List<Denied> = map(
        ::Denied
    )

    fun Iterable<String>.toPermanentlyDeniedPermissions(): List<PermanentlyDenied> = map(
        ::PermanentlyDenied
    )

    fun Iterable<String>.toUndeterminedPermissions(): List<Undetermined> = map(
        ::Undetermined
    )
}
