package uk.gov.onelogin.sharing.bluetooth.api

sealed interface MdocSessionState {
    data object Idle : MdocSessionState
    data object Advertising : MdocSessionState
    data object Stopped : MdocSessionState
    data class Connected(val address: String) : MdocSessionState
    data class Disconnected(val address: String?) : MdocSessionState
    data class Error(val reason: MdocError) : MdocSessionState
}
