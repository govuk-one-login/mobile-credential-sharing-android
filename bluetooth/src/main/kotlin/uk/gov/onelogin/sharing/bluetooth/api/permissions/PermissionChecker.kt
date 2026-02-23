package uk.gov.onelogin.sharing.bluetooth.api.permissions

import android.Manifest
import android.os.Build

/**
 * Output state for the [PermissionChecker] interface and it's inheritance structure.
 */
sealed class PermissionCheckerResult {
    /**
     * State for when all associated permissions are currently granted on the Android-powered
     * device.
     */
    data object Passed : PermissionCheckerResult()

    /**
     * State for when there are required permissions that're currently denied on the Android-powered
     * device.
     *
     * @param missingPermissions The list of [Manifest.permission] permissions that need granting
     * by the User. Defaults to an empty list.
     *
     * @sample Api31BluetoothPermissionChecker.checkPeripheralPermissions
     */
    data class Missing(
        val missingPermissions: List<String> = emptyList()
    ) : PermissionCheckerResult()
}

/**
 * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
 */
fun interface BluetoothPeripheralPermissionChecker {
    /**
     * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
     * This typically includes permissions for advertising and acting as a GATT server.
     *
     * @return [PermissionCheckerResult.Passed] if all required peripheral permissions are granted.
     * Otherwise, [PermissionCheckerResult.Missing] containing the list of required permissions.
     */
    fun checkPeripheralPermissions(): PermissionCheckerResult

    /**
     * Checks if the app has the required permissions to act as a Bluetooth Peripheral.
     * This typically includes permissions for advertising and acting as a GATT server.
     *
     * @return `true` if all required peripheral permissions are granted.
     */
    fun hasPeripheralPermissions(): Boolean =
        checkPeripheralPermissions() == PermissionCheckerResult.Passed

    companion object {
        @JvmStatic
        fun peripheralPermissions(): List<String> = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.BLUETOOTH)
            }
        }
    }
}

/**
 * Checks if the app has the required permissions to act as a Bluetooth Central.
 */
fun interface BluetoothCentralPermissionChecker {
    /**
     * Checks if the app has the required permissions to act as a Bluetooth Central.
     * This typically includes permissions for scanning and connecting to GATT servers.
     *
     * @return [PermissionCheckerResult.Passed] if all required central permissions are granted.
     * Otherwise, [PermissionCheckerResult.Missing] containing the list of required permissions.
     */
    fun checkCentralPermissions(): PermissionCheckerResult
    /**
     * Checks if the app has the required permissions to act as a Bluetooth Central.
     * This typically includes permissions for scanning and connecting to GATT servers.
     *
     * @return `true` if all required central permissions are granted.
     */
    fun hasCentralPermissions(): Boolean =
        checkCentralPermissions() == PermissionCheckerResult.Passed

    companion object {
        @JvmStatic
        fun centralPermissions(): List<String> = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_SCAN)
            } else {
                add(Manifest.permission.ACCESS_FINE_LOCATION)
                add(Manifest.permission.BLUETOOTH)
            }
        }
    }
}

/**
 * Checks if the application has the necessary permissions for Bluetooth operations.
 *
 * This contract separates the permission checks for acting as a Bluetooth Peripheral (server)
 * versus a Bluetooth Central (client).
 */
interface PermissionChecker :
    BluetoothPeripheralPermissionChecker,
    BluetoothCentralPermissionChecker
