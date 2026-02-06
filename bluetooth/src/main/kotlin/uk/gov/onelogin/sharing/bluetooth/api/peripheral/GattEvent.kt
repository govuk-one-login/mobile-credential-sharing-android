package uk.gov.onelogin.sharing.bluetooth.api.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import uk.gov.onelogin.sharing.bluetooth.api.gatt.peripheral.GattServerEvent

sealed interface GattEvent {
    data class ConnectionStateChange(
        val status: Int,
        val newState: Int,
        val device: BluetoothDevice
    ) : GattEvent {
        fun toGattServerEvent(): GattServerEvent {
            val address = device.address

            return when {
                status == BluetoothGatt.GATT_SUCCESS &&
                    newState == BluetoothProfile.STATE_CONNECTED ->
                    GattServerEvent.Connected(address)

                newState == BluetoothProfile.STATE_DISCONNECTED ->
                    GattServerEvent.Disconnected(address)

                else -> GattServerEvent.UnsupportedEvent(
                    device.address,
                    status,
                    newState
                )
            }
        }
    }

    data class ServiceAdded(val status: Int, val service: BluetoothGattService?) : GattEvent
    data class MessageReceived(val byteArray: ByteArray) : GattEvent {
        override fun equals(other: Any?): Boolean {
            val other = other as? MessageReceived ?: return false
            return this.byteArray.contentEquals(other.byteArray)
        }

        override fun hashCode(): Int = byteArray.contentHashCode()
    }

    data object ConnectionStateStarted : GattEvent

    data class MtuChanged(val device: BluetoothDevice?, val mtu: Int) : GattEvent

    sealed interface DescriptorWriteRequest : GattEvent {
        data class Valid(
            val device: BluetoothDevice,
            val requestId: Int,
            val descriptor: BluetoothGattDescriptor,
            val preparedWrite: Boolean,
            val responseNeeded: Boolean,
            val offset: Int,
            val value: ByteArray
        ) : DescriptorWriteRequest

        data class Invalid(val requestId: Int, val responseNeeded: Boolean, val reason: Reason) :
            DescriptorWriteRequest {
            enum class Reason { NullDevice, NullDescriptor, EmptyValue }
        }
    }

    data object SessionEnd : GattEvent
}
