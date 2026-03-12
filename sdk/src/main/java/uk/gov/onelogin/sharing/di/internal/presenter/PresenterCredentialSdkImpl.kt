package uk.gov.onelogin.sharing.di.internal.presenter

import uk.gov.onelogin.CredentialProvider
import uk.gov.onelogin.sharing.di.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialGraph
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

class PresenterCredentialSdkImpl(
    private val appGraph: CredentialSharingAppGraph,
    private val presenterGraphFactory: PresenterCredentialGraph.Factory
) : PresenterCredentialSdk {
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
