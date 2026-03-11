package uk.gov.onelogin.sharing.di

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import uk.gov.onelogin.orchestration.Orchestrator

@DependencyGraph(AppScope::class)
interface VerifierCredentialGraph {

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Includes appGraph: CredentialSharingAppGraph): VerifierCredentialGraph
    }

    fun verifierOrchestrator(): Orchestrator.Verifier
}
