package uk.gov.onelogin.sharing.sdk.api.verifier

import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig

interface VerifyCredentialSdk {
    /**
     * Creates a new [VerificationSession] for the given [verifierConfig].
     *
     * The session owns its lifecycle internally. Pass it to
     * [uk.gov.onelogin.sharing.ui.impl.VerifyCredential] for the full UI flow,
     * or drive it directly for headless usage.
     */
    fun createSession(verifierConfig: VerifierConfig): VerificationSession

    /**
     * Creates a [CredentialVerifier] for the given [verifierConfig].
     *
     * @deprecated Use [createSession] instead. This method exposes internal implementation
     * details (appGraph, orchestrator) on the public API.
     */
    @Deprecated(
        message = "Use createSession() instead. CredentialVerifier exposes internal details.",
        replaceWith = ReplaceWith("createSession(verifierConfig)")
    )
    @Suppress("DEPRECATION")
    fun verifier(verifierConfig: VerifierConfig): CredentialVerifier
}
