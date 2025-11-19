package uk.gov.onelogin.sharing.bluetooth.api

import android.content.Context
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider

object BleAdvertiserFactory {

    fun create(context: Context): BleAdvertiser {
        val adapterProvider = AndroidBluetoothAdapterProvider(context)

        return AndroidBleAdvertiser(
            bleProvider = AndroidBleProvider(
                bluetoothAdapter = adapterProvider,
                bleAdvertiser = AndroidBluetoothAdvertiserProvider(adapterProvider)
            ),
            permissionChecker = BluetoothPermissionChecker(context)
        )
    }
}
