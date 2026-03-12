package uk.gov.onelogin.sharing.di.api.verifier

import uk.gov.onelogin.VerifierConfig

fun interface VerifierCredentialSdk {
    fun verifier(verifierConfig: VerifierConfig): CredentialVerifier
}
