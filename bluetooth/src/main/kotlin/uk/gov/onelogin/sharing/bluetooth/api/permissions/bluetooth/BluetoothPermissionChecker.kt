package uk.gov.onelogin.sharing.bluetooth.api.permissions.bluetooth

import android.Manifest
import android.os.Build
import uk.gov.onelogin.sharing.core.permission.PermissionChecker.Response

/**
 * Checks if the application has the necessary permissions for Bluetooth operations.
 *
 * This contract separates the permission checks for acting as a Bluetooth Peripheral (server)
 * versus a Bluetooth Central (client).
 */
interface BluetoothPermissionChecker :
    BluetoothCentralPermissionChecker {

    fun checkBluetoothPermissions(): Response

    fun hasBluetoothPermissions(): Boolean = checkBluetoothPermissions() == Response.Passed

        companion object {
            @JvmStatic
            fun bluetoothPermissions(): List<String> = buildList {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    add(Manifest.permission.BLUETOOTH_CONNECT)
                    add(Manifest.permission.BLUETOOTH_ADVERTISE)
                    add(Manifest.permission.BLUETOOTH_SCAN)
                } else {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.BLUETOOTH)
                }
            }
        }
    }
