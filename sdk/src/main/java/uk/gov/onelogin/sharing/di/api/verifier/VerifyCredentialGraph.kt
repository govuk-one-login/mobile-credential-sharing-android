package uk.gov.onelogin.sharing.di.api.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import uk.gov.onelogin.VerifierConfig
import uk.gov.onelogin.orchestration.Orchestrator
import uk.gov.onelogin.sharing.di.api.shared.CredentialSharingAppGraph

@DependencyGraph(AppScope::class)
interface VerifyCredentialGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Includes appGraph: CredentialSharingAppGraph,
            @Provides verifierConfig: VerifierConfig
        ): VerifyCredentialGraph
    }

    fun verifierOrchestrator(): Orchestrator.Verifier

    fun verifierConfig(): VerifierConfig
}
