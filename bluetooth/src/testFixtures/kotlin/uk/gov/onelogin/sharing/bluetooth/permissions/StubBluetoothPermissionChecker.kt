package uk.gov.onelogin.sharing.bluetooth.permissions

import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

class StubBluetoothPermissionChecker(var result: Response = Response.Passed) :
    BluetoothPermissionChecker {
    override fun checkBluetoothPermissions(): Response = result
}
