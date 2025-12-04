package uk.gov.onelogin.sharing.bluetooth.api.adapter

import android.bluetooth.le.BluetoothLeAdvertiser

class FakeBluetoothAdapterProvider(
    private var isEnabled: Boolean,
    private val advertiser: BluetoothLeAdvertiser? = null
) : BluetoothAdapterProvider {

    override fun isEnabled(): Boolean = isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? = advertiser

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
