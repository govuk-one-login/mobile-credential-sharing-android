package uk.gov.onelogin.sharing.core.permission

data class StubPermissionChecker(val result: PermissionChecker.Response) : PermissionChecker {
    override fun checkPermissions(permissions: List<String>): PermissionChecker.Response = result
}
