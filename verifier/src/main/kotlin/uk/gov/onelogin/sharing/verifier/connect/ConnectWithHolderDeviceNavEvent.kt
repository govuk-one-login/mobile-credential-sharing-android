package uk.gov.onelogin.sharing.verifier.connect

import uk.gov.onelogin.sharing.core.presentation.bluetooth.BluetoothSessionError

sealed interface ConnectWithHolderDeviceNavEvent {
    data class NavigateToError(val error: BluetoothSessionError) :
        ConnectWithHolderDeviceNavEvent
}
