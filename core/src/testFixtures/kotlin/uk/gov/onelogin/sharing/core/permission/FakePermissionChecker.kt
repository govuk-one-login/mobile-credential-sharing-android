package uk.gov.onelogin.sharing.core.permission

import uk.gov.onelogin.sharing.core.permission.PermissionCheckerV2.PermissionCheckResult

class FakePermissionChecker(
    private val missingPermissions: () -> List<PermissionCheckResult> = { emptyList() }
) : PermissionCheckerV2 {
    override fun checkPermissions(permissions: List<String>): List<PermissionCheckResult> =
        missingPermissions().filter {
            it.permission in permissions
        }
}
