package uk.gov.onelogin.sharing.ui.impl

import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.ui.api.CredentialPresenter

class FakeCredentialPresenter(
    override val orchestrator: Orchestrator.Holder
) : CredentialPresenter
