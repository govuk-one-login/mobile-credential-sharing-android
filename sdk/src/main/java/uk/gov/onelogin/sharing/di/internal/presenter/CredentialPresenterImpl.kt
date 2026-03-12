package uk.gov.onelogin.sharing.di.internal.presenter

import uk.gov.onelogin.CredentialProvider
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

class CredentialPresenterImpl(
    @Suppress("UnusedPrivateProperty")
    private val credentialProvider: CredentialProvider,
    override val orchestrator: Orchestrator.Holder,
    override val appGraph: CredentialSharingAppGraph
) : CredentialPresenter
