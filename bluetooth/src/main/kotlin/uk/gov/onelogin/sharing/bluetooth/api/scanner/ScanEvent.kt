package uk.gov.onelogin.sharing.bluetooth.api.scanner

sealed class ScanEvent {
    data class DeviceFound(val deviceAddress: String) : ScanEvent()
    data class ScanFailed(val failure: ScannerFailure) : ScanEvent()
}
