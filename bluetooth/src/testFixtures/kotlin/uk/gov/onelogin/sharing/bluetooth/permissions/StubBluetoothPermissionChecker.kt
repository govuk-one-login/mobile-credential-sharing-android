package uk.gov.onelogin.sharing.bluetooth.permissions

import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

class StubBluetoothPermissionChecker(
    var peripheralResult: Response = Response.Passed,
    var centralResult: Response = Response.Passed
) : BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): Response = peripheralResult
    override fun checkCentralPermissions(): Response = centralResult
}
