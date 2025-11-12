package uk.gov.onelogin.sharing.bluetooth.internal.util

import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.ContextWrapper

/**
 * Test helper that injects a mocked [BluetoothManager] into a [Context].
 *
 */
class BluetoothContext(base: Context, private val bluetoothManager: BluetoothManager) :
    ContextWrapper(base) {

    override fun getSystemService(name: String): Any? = if (name == BLUETOOTH_SERVICE) {
        bluetoothManager
    } else {
        super.getSystemService(name)
    }
}
