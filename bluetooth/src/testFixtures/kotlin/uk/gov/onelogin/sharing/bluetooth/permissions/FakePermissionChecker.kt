package uk.gov.onelogin.sharing.bluetooth.permissions

import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker

class FakePermissionChecker(
    var peripheralResult: PermissionCheckerResult = PermissionCheckerResult.Passed,
    var centralResult: PermissionCheckerResult = PermissionCheckerResult.Passed,
) : BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult = peripheralResult
    override fun checkCentralPermissions(): PermissionCheckerResult = centralResult
}
