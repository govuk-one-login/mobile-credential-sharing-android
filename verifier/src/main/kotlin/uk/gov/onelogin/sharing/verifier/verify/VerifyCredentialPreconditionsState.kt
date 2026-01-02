package uk.gov.onelogin.sharing.verifier.verify

sealed class VerifyCredentialPreconditionsState {
    class BluetoothDisabled : VerifyCredentialPreconditionsState()
    class BluetoothAccessDenied : VerifyCredentialPreconditionsState()

    class Met : VerifyCredentialPreconditionsState()
}
