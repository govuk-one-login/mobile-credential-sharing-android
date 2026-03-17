package uk.gov.onelogin.sharing.sdk.api.verifier

import uk.gov.onelogin.sharing.orchestration.VerifierConfig

fun interface VerifyCredentialSdk {
    fun verifier(verifierConfig: VerifierConfig): CredentialVerifier
}
