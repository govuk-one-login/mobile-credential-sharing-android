package uk.gov.onelogin.sharing.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context

object ContextExt {
    /**
     * [Context] extension function that requests the [Context.BLUETOOTH_SERVICE] as an instance of
     * [BluetoothManager].
     *
     * @return A [BluetoothManager] instance if the [Context] has one. Otherwise, `null`.
     */
    val Context.bluetoothManager: BluetoothManager? get() = getSystemService(
        Context.BLUETOOTH_SERVICE
    ) as? BluetoothManager
}
