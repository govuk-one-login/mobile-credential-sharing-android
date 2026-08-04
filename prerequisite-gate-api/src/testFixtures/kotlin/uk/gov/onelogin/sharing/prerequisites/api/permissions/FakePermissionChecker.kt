package uk.gov.onelogin.sharing.prerequisites.permissions

import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.prerequisites.api.permissions.PermissionChecker.PermissionCheckResult

class FakePermissionChecker(
    private val missingPermissions: () -> List<PermissionCheckResult> = { emptyList() }
) : PermissionChecker {

    val markedPermissions: MutableList<String> = mutableListOf()

    override fun checkPermissions(permissions: List<String>): List<PermissionCheckResult> =
        missingPermissions().filter {
            it.permission in permissions
        }

    override fun markAsRequested(permissions: List<String>) {
        markedPermissions.addAll(permissions)
    }
}
