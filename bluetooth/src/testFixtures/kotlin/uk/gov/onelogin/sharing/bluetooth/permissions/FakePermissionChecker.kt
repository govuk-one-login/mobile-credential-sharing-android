package uk.gov.onelogin.sharing.bluetooth.permissions

import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker

class FakePermissionChecker(var hasPermission: Boolean = true) : PermissionChecker {

    override fun hasPermission(): Boolean = hasPermission
}
