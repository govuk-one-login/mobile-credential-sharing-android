package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal class AndroidBluetoothStateMonitor(private val appContext: Context) :
    BluetoothStateMonitor {
    private val _states = MutableSharedFlow<BluetoothStatus>(
        replay = 1
    )
    override val states: SharedFlow<BluetoothStatus> = _states

    private val receiver = object : BroadcastReceiver() {
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

            _states.tryEmit(state)
        }
    }

    override fun start() {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        val broadcastPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Manifest.permission.BLUETOOTH_CONNECT
            } else {
                Manifest.permission.BLUETOOTH
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(
                receiver,
                filter,
                broadcastPermission,
                null,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            appContext.registerReceiver(
                receiver,
                filter,
                broadcastPermission,
                null
            )
        }

        val adapter = appContext
            .getSystemService(BluetoothManager::class.java)
            ?.adapter

        val initialState = if (adapter?.isEnabled == true) {
            BluetoothStatus.ON
        } else {
            BluetoothStatus.OFF
        }
        _states.tryEmit(initialState)
    }

    override fun stop() {
        try {
            appContext.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            println(e.message)
        }
    }
}
