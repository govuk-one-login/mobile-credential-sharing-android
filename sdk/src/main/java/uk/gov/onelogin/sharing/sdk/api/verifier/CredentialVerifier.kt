package uk.gov.onelogin.sharing.sdk.api.verifier

import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

/**
 * @deprecated Use [VerificationSession] instead. This interface exposes internal SDK
 * implementation details (appGraph, orchestrator) on the public API surface.
 */
@Deprecated(
    message = "Use VerificationSession from createSession() instead. " +
        "CredentialVerifier exposes internal SDK details.",
    replaceWith = ReplaceWith("VerificationSession")
)
interface CredentialVerifier {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Verifier
}
