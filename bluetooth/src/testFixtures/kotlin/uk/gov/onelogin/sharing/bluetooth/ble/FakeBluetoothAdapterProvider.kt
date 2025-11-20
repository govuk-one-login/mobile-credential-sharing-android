package uk.gov.onelogin.sharing.bluetooth.ble

import android.bluetooth.le.BluetoothLeAdvertiser
import uk.gov.onelogin.sharing.bluetooth.internal.core.BluetoothAdapterProvider

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
