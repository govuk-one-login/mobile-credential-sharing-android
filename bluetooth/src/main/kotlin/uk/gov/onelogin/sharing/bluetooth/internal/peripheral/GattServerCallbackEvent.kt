package uk.gov.onelogin.sharing.bluetooth.internal.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import java.util.UUID
import uk.gov.onelogin.sharing.bluetooth.api.peripheral.GattServerEvent

internal sealed interface GattServerCallbackEvent {
    data class ConnectionStateChange(
        val status: Int,
        val newState: Int,
        val device: BluetoothDevice
    ) : GattServerCallbackEvent {
        fun toGattServerEvent(isSessionEnd: Boolean): GattServerEvent {
            val address = device.address

            return when {
                status == BluetoothGatt.GATT_SUCCESS &&
                    newState == BluetoothProfile.STATE_CONNECTED ->
                    GattServerEvent.Connected(address)

                newState == BluetoothProfile.STATE_DISCONNECTED ->
                    GattServerEvent.Disconnected(address, isSessionEnd)

                else -> GattServerEvent.UnsupportedEvent(
                    device.address,
                    status,
                    newState
                )
            }
        }
    }

    data class ServiceAdded(val status: Int, val service: BluetoothGattService) :
        GattServerCallbackEvent

    /** Raw characteristic write (ISO header byte + payload); interpreted by the manager. */
    data class CharacteristicWriteRequest(
        val device: BluetoothDevice,
        val characteristicUuid: UUID,
        val value: ByteArray
    ) : GattServerCallbackEvent {
        override fun equals(other: Any?): Boolean {
            val other = other as? CharacteristicWriteRequest ?: return false
            return this.device == other.device &&
                this.characteristicUuid == other.characteristicUuid &&
                this.value.contentEquals(other.value)
        }

        override fun hashCode(): Int {
            var result = device.hashCode()
            result = 31 * result + characteristicUuid.hashCode()
            result = 31 * result + value.contentHashCode()
            return result
        }
    }

    data class MtuChanged(val device: BluetoothDevice?, val mtu: Int) : GattServerCallbackEvent

    sealed interface DescriptorWriteRequest : GattServerCallbackEvent {
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
}
