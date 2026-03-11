package uk.gov.onelogin.sharing.ui.api

import uk.gov.onelogin.orchestration.Orchestrator

/**
 * Holder role: Presents credentials to verifiers.
 */
interface CredentialPresenter {
    val orchestrator: Orchestrator.Holder
}
