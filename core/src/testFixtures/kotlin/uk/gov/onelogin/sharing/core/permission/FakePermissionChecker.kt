package uk.gov.onelogin.sharing.core.permission

import uk.gov.onelogin.sharing.core.permission.PermissionChecker.PermissionCheckResult

class FakePermissionChecker(
    private val missingPermissions: () -> List<PermissionCheckResult> = { emptyList() }
) : PermissionChecker {
    override fun checkPermissions(permissions: List<String>): List<PermissionCheckResult> =
        missingPermissions().filter {
            it.permission in permissions
        }
}
