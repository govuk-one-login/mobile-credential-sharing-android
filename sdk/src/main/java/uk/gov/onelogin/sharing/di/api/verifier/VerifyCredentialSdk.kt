package uk.gov.onelogin.sharing.di.api.verifier

import uk.gov.onelogin.VerifierConfig

fun interface VerifyCredentialSdk {
    fun verifier(verifierConfig: VerifierConfig): CredentialVerifier
}
