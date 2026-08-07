package uk.gov.onelogin.sharing.sdk.api.presenter

import uk.gov.onelogin.sharing.core.SharingSessionScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import uk.gov.onelogin.sharing.orchestration.CredentialProvider
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

@DependencyGraph(SharingSessionScope::class)
interface PresentCredentialGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides credentialProvider: CredentialProvider
        ): PresentCredentialGraph
    }

    fun holderOrchestrator(): Orchestrator.Holder

    fun credentialProvider(): CredentialProvider
}
