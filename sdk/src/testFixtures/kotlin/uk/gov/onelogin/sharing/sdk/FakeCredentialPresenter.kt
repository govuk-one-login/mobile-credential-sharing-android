package uk.gov.onelogin.sharing.sdk

import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

class FakeCredentialPresenter(
    override val appGraph: CredentialSharingAppGraph,
    override val orchestrator: Orchestrator.Holder
) : CredentialPresenter
