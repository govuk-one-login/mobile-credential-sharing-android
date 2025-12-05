package uk.gov.onelogin.sharing.bluetooth.internal.client

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.SharedFlow
import uk.gov.onelogin.sharing.bluetooth.internal.peripheral.MdocState

internal interface GattClientManager {
    val events: SharedFlow<GattClientEvent>

    fun connect(device: BluetoothDevice)

    fun disconnect()

    fun writeState(command: MdocState)
    }