package uk.gov.onelogin.sharing.verifier.verify

sealed interface VerifyCredentialPreconditionsState {
    data object Idle : VerifyCredentialPreconditionsState
    data object BluetoothDisabled : VerifyCredentialPreconditionsState
    data object BluetoothAccessDenied : VerifyCredentialPreconditionsState

    data object Met : VerifyCredentialPreconditionsState
}
