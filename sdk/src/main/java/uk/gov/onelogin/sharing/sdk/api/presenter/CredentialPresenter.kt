package uk.gov.onelogin.sharing.sdk.api.presenter

import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

interface CredentialPresenter {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Holder
}
