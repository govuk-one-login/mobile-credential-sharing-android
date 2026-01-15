package uk.gov.onelogin.sharing.verifier.session

sealed interface VerifierSessionState {
    data object Idle : VerifierSessionState
    data object Starting : VerifierSessionState
    data object Connecting : VerifierSessionState
    data class Connected(val address: String) : VerifierSessionState
    data class Disconnected(val address: String) : VerifierSessionState
    data object ConnectionStateStarted : VerifierSessionState
    data class Error(val message: String) : VerifierSessionState

    /**
     * [VerifierSessionState] implementation for when bluetooth configuration is incorrect.
     *
     * This occurs due to [android.bluetooth.BluetoothGattCharacteristic] issues.
     */
    data object Invalid : VerifierSessionState
}
