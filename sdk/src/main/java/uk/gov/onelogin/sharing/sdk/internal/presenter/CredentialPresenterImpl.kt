package uk.gov.onelogin.sharing.sdk.internal.presenter

import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

class CredentialPresenterImpl(
    @Suppress("UnusedPrivateProperty")
    private val credentialProvider: CredentialProvider,
    override val orchestrator: Orchestrator.Holder,
    override val appGraph: CredentialSharingAppGraph
) : CredentialPresenter
