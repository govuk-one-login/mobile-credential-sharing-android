package uk.gov.onelogin.sharing.bluetooth.api.adapter

import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metrox.viewmodel.ViewModelScope
import uk.gov.onelogin.sharing.bluetooth.ContextExt.bluetoothManager

@ContributesBinding(ViewModelScope::class)
class AndroidBluetoothAdapterProvider(val context: Context) : BluetoothAdapterProvider {
    private val bluetoothManager by lazy { context.bluetoothManager!! }

    override fun isEnabled(): Boolean = bluetoothManager.adapter.isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? =
        bluetoothManager.adapter.bluetoothLeAdvertiser

    override fun getLeScanner(): BluetoothLeScanner? = bluetoothManager.adapter.bluetoothLeScanner
}
