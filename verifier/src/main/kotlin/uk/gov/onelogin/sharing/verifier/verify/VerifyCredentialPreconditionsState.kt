package uk.gov.onelogin.sharing.verifier.verify

sealed class VerifyCredentialPreconditionsState {
    data object BluetoothDisabled : VerifyCredentialPreconditionsState()
    data object BluetoothAccessDenied : VerifyCredentialPreconditionsState()

    data object Met : VerifyCredentialPreconditionsState()
}
