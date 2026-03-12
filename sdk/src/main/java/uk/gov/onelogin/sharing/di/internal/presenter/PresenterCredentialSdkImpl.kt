package uk.gov.onelogin.sharing.di.internal.presenter

import dev.zacsweers.metro.createGraphFactory
import uk.gov.onelogin.sharing.di.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.di.api.presenter.CredentialProvider
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialGraph
import uk.gov.onelogin.sharing.di.api.presenter.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

class PresenterCredentialSdkImpl(val appGraph: CredentialSharingAppGraph) :
    PresenterCredentialSdk {
    override fun presenter(credentialProvider: CredentialProvider): CredentialPresenter {
        val orchestrator = createGraphFactory<PresenterCredentialGraph.Factory>()
            .create(appGraph, credentialProvider)
            .holderOrchestrator()

        return CredentialPresenterImpl(
            credentialProvider = credentialProvider,
            orchestrator = orchestrator,
            appGraph = appGraph
        )
    }
}
