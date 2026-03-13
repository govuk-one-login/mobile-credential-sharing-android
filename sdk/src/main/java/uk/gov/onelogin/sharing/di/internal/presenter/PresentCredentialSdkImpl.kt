package uk.gov.onelogin.sharing.di.internal.presenter

import uk.gov.onelogin.CredentialProvider
import uk.gov.onelogin.sharing.di.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.di.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.di.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

class PresentCredentialSdkImpl(
    private val appGraph: CredentialSharingAppGraph,
    private val presenterGraphFactory: PresentCredentialGraph.Factory
) : PresentCredentialSdk {
    override fun presenter(credentialProvider: CredentialProvider): CredentialPresenter {
        val orchestrator = presenterGraphFactory
            .create(appGraph, credentialProvider)
            .holderOrchestrator()

        return CredentialPresenterImpl(
            credentialProvider = credentialProvider,
            orchestrator = orchestrator,
            appGraph = appGraph
        )
    }
}
