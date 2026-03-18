package uk.gov.onelogin.sharing.sdk.internal.presenter

import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.sdk.api.presenter.CredentialPresenter
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialGraph
import uk.gov.onelogin.sharing.sdk.api.presenter.PresentCredentialSdk
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

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
