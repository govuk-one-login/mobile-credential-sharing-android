package uk.gov.onelogin.sharing.bluetooth.ble

import android.bluetooth.BluetoothDevice
import io.mockk.every
import io.mockk.mockk

const val DEVICE_ADDRESS = "AA:BB:CC:DD:EE:FF"

fun mockBluetoothDevice(address: String = DEVICE_ADDRESS): BluetoothDevice =
    mockk<BluetoothDevice>().also {
        every { it.address } returns address
    }
