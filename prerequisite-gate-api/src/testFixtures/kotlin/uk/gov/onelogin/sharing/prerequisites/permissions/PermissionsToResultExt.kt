package uk.gov.onelogin.sharing.prerequisites.permissions

import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionChecker.PermissionCheckResult.Denied
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionChecker.PermissionCheckResult.PermanentlyDenied
import uk.gov.onelogin.sharing.prerequisites.permissions.PermissionChecker.PermissionCheckResult.Undetermined

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
