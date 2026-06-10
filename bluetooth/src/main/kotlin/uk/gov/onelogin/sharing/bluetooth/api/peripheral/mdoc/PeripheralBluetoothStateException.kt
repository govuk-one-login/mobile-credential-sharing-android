package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

class PeripheralBluetoothStateException(
    val error: PeripheralBluetoothTransportError
) : Exception(error.message)
