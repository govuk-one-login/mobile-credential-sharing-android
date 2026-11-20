package uk.gov.onelogin.sharing.holder.qrcode

sealed class BluetoothState {
    data object Enabled : BluetoothState()
    data object Disabled : BluetoothState()
    data object Initializing : BluetoothState()
    data object Unknown : BluetoothState()
}
