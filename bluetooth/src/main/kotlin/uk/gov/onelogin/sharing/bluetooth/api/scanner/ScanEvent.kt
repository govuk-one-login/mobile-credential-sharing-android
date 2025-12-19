package uk.gov.onelogin.sharing.bluetooth.api.scanner

import android.bluetooth.BluetoothDevice

sealed class ScanEvent {
    data class DeviceFound(val device: BluetoothDevice) : ScanEvent()
    data class ScanFailed(val failure: ScannerFailure) : ScanEvent()
}
