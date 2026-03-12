package uk.gov.onelogin.sharing.di.api.presenter

import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

interface CredentialPresenter {
    val appGraph: CredentialSharingAppGraph

    val orchestrator: Orchestrator.Holder
}
