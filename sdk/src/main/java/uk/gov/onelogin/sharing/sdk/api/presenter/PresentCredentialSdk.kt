package uk.gov.onelogin.sharing.sdk.api.presenter

import uk.gov.onelogin.sharing.orchestration.CredentialProvider

interface PresentCredentialSdk {
    /**
     * Creates a new [SharingSession] for the given [credentialProvider].
     *
     * The session owns its lifecycle internally. Pass it to
     * [uk.gov.onelogin.sharing.ui.impl.ShareCredential] for the full UI flow,
     * or drive it directly for headless usage.
     */
    fun createSession(credentialProvider: CredentialProvider): SharingSession

    /**
     * Creates a [CredentialPresenter] for the given [credentialProvider].
     *
     * @deprecated Use [createSession] instead. This method exposes internal implementation
     * details (appGraph, orchestrator) on the public API.
     */
    @Deprecated(
        message = "Use createSession() instead. CredentialPresenter exposes internal details.",
        replaceWith = ReplaceWith("createSession(credentialProvider)")
    )
    @Suppress("DEPRECATION")
    fun presenter(credentialProvider: CredentialProvider): CredentialPresenter
}
