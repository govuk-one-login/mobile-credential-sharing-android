package uk.gov.onelogin.sharing.holder.presentation

sealed class BluetoothState {
    data object Enabled: BluetoothState()
    data object Disabled: BluetoothState()
    data object Initializing: BluetoothState()
}