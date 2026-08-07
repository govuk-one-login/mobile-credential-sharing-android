package uk.gov.onelogin.sharing.sdk.api.presenter

import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

/**
 * @deprecated Use [SharingSession] instead. This interface exposes internal SDK
 * implementation details (appGraph, orchestrator) on the public API surface.
 */
@Deprecated(
    message = "Use SharingSession from createSession() instead. " +
        "CredentialPresenter exposes internal SDK details.",
    replaceWith = ReplaceWith("SharingSession")
)
interface CredentialPresenter {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Holder
}
