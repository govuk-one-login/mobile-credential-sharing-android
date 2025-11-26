package uk.gov.onelogin.sharing.bluetooth.api

import android.bluetooth.BluetoothManager
import android.content.Context
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.AndroidMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.AndroidGattServerManager

/**
 * A factory for creating a [AndroidMdocSessionManager].
 *
 * Encapsulates the creation and dependency wiring of the components
 * required for the BLE advertiser and GATT server.
 */
object MdocSessionFactory {
    /**
     *
     * Constructs and configures all the necessary dependencies for a
     * [AndroidMdocSessionManager].
     *
     * @param context The Android [Context] used to access system services like the
     * [BluetoothManager].
     * @return A fully configured [MdocSessionManager] instance.
     */
    fun create(context: Context): MdocSessionManager {
        val adapterProvider = AndroidBluetoothAdapterProvider(context)
        val bleAdvertiser = AndroidBleAdvertiser(
            bleProvider = AndroidBleProvider(
                bluetoothAdapter = adapterProvider,
                bleAdvertiser = AndroidBluetoothAdvertiserProvider(adapterProvider)
            ),
            permissionChecker = BluetoothPermissionChecker(context)
        )
        val gattServerManager = AndroidGattServerManager(
            context = context,
            bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        )
        return AndroidMdocSessionManager(
            bleAdvertiser,
            gattServerManager
        )
    }
}
