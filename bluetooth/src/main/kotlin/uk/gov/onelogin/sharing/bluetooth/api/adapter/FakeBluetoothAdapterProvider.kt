package uk.gov.onelogin.sharing.bluetooth.api.adapter

import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner

class FakeBluetoothAdapterProvider(
    private var isEnabled: Boolean,
    private val advertiser: BluetoothLeAdvertiser? = null
) : BluetoothAdapterProvider {

    override fun isEnabled(): Boolean = isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? = advertiser

    override fun getLeScanner(): BluetoothLeScanner? = null

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
