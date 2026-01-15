package uk.gov.onelogin.sharing.verifier.connect

sealed interface ConnectWithHolderDeviceNavEvent {
    data class NavigateToError(val error: ConnectWithHolderDeviceError) :
        ConnectWithHolderDeviceNavEvent
}
