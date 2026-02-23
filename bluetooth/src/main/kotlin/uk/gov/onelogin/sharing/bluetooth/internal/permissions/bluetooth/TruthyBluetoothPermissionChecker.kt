package uk.gov.onelogin.sharing.bluetooth.internal.permissions.bluetooth

import uk.gov.onelogin.sharing.bluetooth.api.permissions.PermissionCheckerResult
import uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker

/**
 * [uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth.BluetoothPermissionChecker] implementation for use in Android-powered devices lower than
 * [android.os.Build.VERSION_CODES.S].
 */
internal data object TruthyBluetoothPermissionChecker : BluetoothPermissionChecker {
    override fun checkPeripheralPermissions(): PermissionCheckerResult =
        PermissionCheckerResult.Passed

    override fun checkCentralPermissions(): PermissionCheckerResult =
        PermissionCheckerResult.Passed
}