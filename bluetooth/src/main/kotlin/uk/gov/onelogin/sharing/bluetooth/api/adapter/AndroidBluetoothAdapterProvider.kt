package uk.gov.onelogin.sharing.bluetooth.api.adapter

import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelScope

@ContributesBinding(ViewModelScope::class)
@Inject
class AndroidBluetoothAdapterProvider(val context: Context) : BluetoothAdapterProvider {
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    override fun isEnabled(): Boolean = bluetoothManager.adapter.isEnabled

    override fun getAdvertiser(): BluetoothLeAdvertiser? =
        bluetoothManager.adapter.bluetoothLeAdvertiser

    override fun getLeScanner(): BluetoothLeScanner? = bluetoothManager.adapter.bluetoothLeScanner
}
