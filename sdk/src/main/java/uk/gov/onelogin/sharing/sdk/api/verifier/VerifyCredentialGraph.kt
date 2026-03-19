package uk.gov.onelogin.sharing.sdk.api.verifier

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Includes
import dev.zacsweers.metro.Provides
import uk.gov.onelogin.sharing.orchestration.Orchestrator
import uk.gov.onelogin.sharing.orchestration.verificationrequest.VerifierConfig
import uk.gov.onelogin.sharing.sdk.api.shared.CredentialSharingAppGraph

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
