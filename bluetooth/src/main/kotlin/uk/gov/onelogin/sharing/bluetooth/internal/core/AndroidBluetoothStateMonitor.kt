package uk.gov.onelogin.sharing.bluetooth.internal.core

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.os.Build
import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.logging.api.v2.Logger
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStateMonitor
import uk.gov.onelogin.sharing.bluetooth.api.core.BluetoothStatus
import uk.gov.onelogin.sharing.core.VerifierUiScope
import uk.gov.onelogin.sharing.core.logger.logTag

@ContributesBinding(VerifierUiScope::class)
@ContributesBinding(SharingSessionScope::class)
class AndroidBluetoothStateMonitor(private val appContext: Context, private val logger: Logger) :
    BluetoothStateMonitor {
    private val _states = MutableSharedFlow<BluetoothStatus>(
        replay = 1
    )
    override val states: SharedFlow<BluetoothStatus> = _states
    private var isRegistered = false

    private val receiver = BluetoothStateBroadcastReceiver(_states::tryEmit)

    override fun start() {
        if (isRegistered) return

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

        isRegistered = true

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
        if (!isRegistered) return
        isRegistered = false

        try {
            appContext.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            logger.error(logTag, e.message ?: "Illegal argument exception", e)
        }
    }
}
