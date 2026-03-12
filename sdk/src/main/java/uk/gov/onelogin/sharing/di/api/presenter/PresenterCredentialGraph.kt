package uk.gov.onelogin.sharing.di.api.presenter

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import uk.gov.onelogin.CredentialProvider
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

@DependencyGraph(AppScope::class)
interface PresenterCredentialGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides credentialProvider: CredentialProvider
        ): PresenterCredentialGraph
    }

    fun holderOrchestrator(): Orchestrator.Holder

    fun credentialProvider(): CredentialProvider
}
