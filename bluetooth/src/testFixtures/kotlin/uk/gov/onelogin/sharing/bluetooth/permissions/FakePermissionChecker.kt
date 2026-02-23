package uk.gov.onelogin.sharing.bluetooth.permissions

import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionChecker
import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult

class FakePermissionChecker(
    var peripheralResult: PermissionCheckerResult = PermissionCheckerResult.Passed,
    var centralResult: PermissionCheckerResult = PermissionCheckerResult.Passed,
) : PermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult = peripheralResult
    override fun checkCentralPermissions(): PermissionCheckerResult = centralResult
}
