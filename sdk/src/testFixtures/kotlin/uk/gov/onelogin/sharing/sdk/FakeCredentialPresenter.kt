package uk.gov.onelogin.sharing.sdk

import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

class FakeCredentialPresenter(
    override val appGraph: CredentialSharingAppGraph,
    override val orchestrator: Orchestrator.Holder
) : CredentialPresenter
