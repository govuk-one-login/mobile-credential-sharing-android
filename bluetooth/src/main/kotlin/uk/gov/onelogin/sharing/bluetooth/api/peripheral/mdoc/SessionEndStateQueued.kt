package uk.gov.onelogin.sharing.bluetooth.api.peripheral.mdoc

sealed interface SessionEndStateQueued {
    data object Success : SessionEndStateQueued
    data object Failed : SessionEndStateQueued
    data object NoDeviceConnected : SessionEndStateQueued
}
