package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.le.BluetoothLeAdvertiser

class FakeBluetoothAdapterProvider(private val isEnabled: Boolean) : BluetoothAdapterProvider {

    override fun isEnabled(): Boolean = isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? = null
}
