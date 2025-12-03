package uk.gov.onelogin.sharing.bluetooth.peripheral

import android.Manifest
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.annotation.RequiresPermission
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

object GattServerMock {
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setupOpenGattServer(
        bluetoothManager: BluetoothManager,
        context: Context,
        gattServer: BluetoothGattServer = mockk(relaxed = true)
    ): Pair<CapturingSlot<BluetoothGattServerCallback>, BluetoothGattServer> {
        val callbackSlot = slot<BluetoothGattServerCallback>()
        every {
            bluetoothManager.openGattServer(context, capture(callbackSlot))
        } returns gattServer
        return callbackSlot to gattServer
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun setupNullGattServer(bluetoothManager: BluetoothManager, context: Context) {
        every {
            bluetoothManager.openGattServer(context, any())
        } returns null
    }
}
