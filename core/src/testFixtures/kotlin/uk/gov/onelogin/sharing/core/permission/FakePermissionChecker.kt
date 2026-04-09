package uk.gov.onelogin.sharing.core.permission

class FakePermissionChecker(
    private val missingPermissions: () -> List<PermissionCheckerV2.Denied> = { emptyList() }
) : PermissionCheckerV2 {
    override fun checkPermissions(permissions: List<String>): List<PermissionCheckerV2.Denied> =
        missingPermissions().filter {
            it.permission in permissions
        }
}
