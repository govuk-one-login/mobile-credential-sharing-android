package uk.gov.onelogin.sharing.sdk.api.verifier

import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig

fun interface VerifyCredentialSdk {
    /**
     * Creates a [CredentialVerifier] for the given [verifierConfig].
     *
     * This calls the consumer-supplied [ReaderAuthCredentialProvider.Factory.create], which may
     * perform blocking I/O or CPU-bound work (for example, reading and parsing a reader-auth key
     * and certificate chain).
     * If your `create()` implementation can block, call this function off the main thread.
     *
     * @param verifierConfig configuration including the verification request and trusted root.
     * @return a [CredentialVerifier] bound to the supplied configuration.
     */
    fun verifier(verifierConfig: VerifierConfig): CredentialVerifier
}
