package uk.gov.onelogin.sharing.bluetooth.api

sealed interface MdocSessionState {
    data object Idle : MdocSessionState
    data object Starting : MdocSessionState
    data object Started : MdocSessionState
    data object Stopping : MdocSessionState
    data object Stopped : MdocSessionState
    data class Error(val reason: MdocError) : MdocSessionState
}
