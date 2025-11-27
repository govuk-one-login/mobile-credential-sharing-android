package uk.gov.onelogin.sharing.bluetooth.api

import android.content.Context
import uk.gov.onelogin.sharing.bluetooth.api.permissions.BluetoothPermissionChecker
import uk.gov.onelogin.sharing.bluetooth.internal.AndroidMdocSessionManager
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBleAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.advertising.AndroidBluetoothAdvertiserProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBleProvider
import uk.gov.onelogin.sharing.bluetooth.internal.core.AndroidBluetoothAdapterProvider

object MdocSessionFactory {
    fun create(context: Context): MdocSessionManager {
        val adapterProvider = AndroidBluetoothAdapterProvider(context)
        val bleAdvertiser = AndroidBleAdvertiser(
            bleProvider = AndroidBleProvider(
                bluetoothAdapter = adapterProvider,
                bleAdvertiser = AndroidBluetoothAdvertiserProvider(adapterProvider)
            ),
            permissionChecker = BluetoothPermissionChecker(context)
        )
        return AndroidMdocSessionManager(bleAdvertiser)
    }
}
