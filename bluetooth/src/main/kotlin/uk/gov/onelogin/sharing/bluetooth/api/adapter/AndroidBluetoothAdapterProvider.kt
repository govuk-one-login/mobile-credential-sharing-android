package uk.gov.onelogin.sharing.bluetooth.api.adapter

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context

class AndroidBluetoothAdapterProvider(val context: Context) : BluetoothAdapterProvider {
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    override fun isEnabled(): Boolean = bluetoothManager.adapter.isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? =
        bluetoothManager.adapter.bluetoothLeAdvertiser
}
