package uk.gov.onelogin.sharing.verifier.connect

import android.bluetooth.BluetoothDevice
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import java.util.UUID

sealed interface ConnectWithHolderDeviceEvent {
    data class ConnectToDevice(val device: BluetoothDevice, val serviceUuid: UUID) :
        ConnectWithHolderDeviceEvent

    data class RequestedPermission(val hasRequestedPermission: Boolean) :
        ConnectWithHolderDeviceEvent

    data class UpdatePermission
    @OptIn(ExperimentalPermissionsApi::class)
    constructor(
        val state: MultiplePermissionsState
    ) : ConnectWithHolderDeviceEvent
}
