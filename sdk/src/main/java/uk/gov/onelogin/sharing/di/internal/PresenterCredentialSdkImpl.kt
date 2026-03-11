package uk.gov.onelogin.sharing.di.internal

import dev.zacsweers.metro.createGraphFactory
import uk.gov.onelogin.orchestration.CredentialProviderNewImpl
import uk.gov.onelogin.sharing.CredentialPresenterNew
import uk.gov.onelogin.sharing.CredentialPresenterNewImpl
import uk.gov.onelogin.sharing.PresenterCredentialSdk
import uk.gov.onelogin.sharing.di.CredentialSharingAppGraph
import uk.gov.onelogin.sharing.di.PresenterCredentialGraph

class PresenterCredentialSdkImpl(val appGraph: CredentialSharingAppGraph) :
    PresenterCredentialSdk {
    override fun presenter(credentialProvider: CredentialProviderNewImpl): CredentialPresenterNew {
        val orchestrator = createGraphFactory<PresenterCredentialGraph.Factory>()
            .create(appGraph, credentialProvider)
            .holderOrchestrator()

        return CredentialPresenterNewImpl(
            credentialProvider = credentialProvider,
            orchestrator = orchestrator,
            appGraph = appGraph
        )
    }


}