package uk.gov.onelogin.sharing.bluetooth.permissions

import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker

class FakePermissionChecker(
    var hasPeripheralPermissions: Boolean = true,
    var hasCentralPermissions: Boolean = true
) : PermissionChecker {

    override fun hasPeripheralPermissions(): Boolean = hasPeripheralPermissions

    override fun hasCentralPermissions(): Boolean = hasCentralPermissions
}
