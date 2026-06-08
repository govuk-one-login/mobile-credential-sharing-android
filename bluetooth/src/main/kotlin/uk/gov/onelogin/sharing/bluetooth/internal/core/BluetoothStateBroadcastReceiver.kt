package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus

class BluetoothStateBroadcastReceiver(private val onStateChange: (BluetoothStatus) -> Unit = {}) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return

        val state = when (
            intent.getIntExtra(
                BluetoothAdapter.EXTRA_STATE,
                BluetoothAdapter.ERROR
            )
        ) {
            BluetoothAdapter.STATE_ON -> BluetoothStatus.ON
            BluetoothAdapter.STATE_OFF -> BluetoothStatus.OFF
            BluetoothAdapter.STATE_TURNING_ON -> BluetoothStatus.TURNING_ON
            BluetoothAdapter.STATE_TURNING_OFF -> BluetoothStatus.TURNING_OFF
            else -> BluetoothStatus.UNKNOWN
        }

        onStateChange(state)
    }
}
