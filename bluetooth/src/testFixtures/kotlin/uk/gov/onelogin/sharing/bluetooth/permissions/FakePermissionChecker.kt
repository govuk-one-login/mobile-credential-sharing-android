package uk.gov.onelogin.sharing.bluetooth.permissions

class FakePermissionChecker(var hasPermission: Boolean = true) : PermissionChecker {

    override fun hasPermission(): Boolean = hasPermission
}
