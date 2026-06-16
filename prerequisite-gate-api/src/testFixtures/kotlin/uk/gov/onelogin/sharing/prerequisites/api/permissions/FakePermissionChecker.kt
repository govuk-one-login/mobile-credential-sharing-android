package uk.gov.onelogin.sharing.prerequisites.permissions

import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker.PermissionCheckResult

class FakePermissionChecker(
    private val missingPermissions: () -> List<PermissionCheckResult> = { emptyList() }
) : PermissionChecker {
    override fun checkPermissions(permissions: List<String>): List<PermissionCheckResult> =
        missingPermissions().filter {
            it.permission in permissions
        }
}
