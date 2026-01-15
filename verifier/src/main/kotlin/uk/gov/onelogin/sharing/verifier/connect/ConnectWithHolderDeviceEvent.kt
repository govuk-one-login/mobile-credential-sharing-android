package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState

sealed interface ConnectWithHolderDeviceEvent {
    data class ConnectToDevice(val device: BluetoothDevice, val serviceUuid: ByteArray) :
        ConnectWithHolderDeviceEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ConnectToDevice

            if (device != other.device) return false
            return serviceUuid.contentEquals(other.serviceUuid)
        }

        override fun hashCode(): Int {
            var result = device.hashCode()
            result = 31 * result + serviceUuid.contentHashCode()
            return result
        }
    }

    data class RequestedPermission(val hasRequestedPermission: Boolean) :
        ConnectWithHolderDeviceEvent

    data class StartScanning(val uuid: ByteArray) : ConnectWithHolderDeviceEvent {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as StartScanning

            return uuid.contentEquals(other.uuid)
        }

        override fun hashCode(): Int = uuid.contentHashCode()
    }

    data object StopScanning : ConnectWithHolderDeviceEvent

    data class UpdateEngagementData(val base64EncodedEngagement: String) :
        ConnectWithHolderDeviceEvent

    data class UpdatePermission
    @OptIn(ExperimentalPermissionsApi::class)
    constructor(
        val state: MultiplePermissionsState
    ) : ConnectWithHolderDeviceEvent
}
